package com.zcy.forum.aspect;

import com.zcy.forum.annotation.RateLimit;
import com.zcy.forum.utils.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * 限流切面
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    // Lua脚本：原子性判断+计数（避免并发问题）
    private static final String LIMIT_SCRIPT = """
            local key = KEYS[1]
            local count = tonumber(ARGV[1])
            local period = tonumber(ARGV[2])
            local current = tonumber(redis.call('get', key) or "0")
            if current + 1 > count then
                return false
            else
                redis.call('incr', key)
                redis.call('expire', key, period)
                return true
            end
            """;

    @Around("@annotation(com.zcy.forum.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // 2. 生成限流key
        String key = generateKey(rateLimit, joinPoint);
        int count = rateLimit.count();
        int period = rateLimit.period();

        // 3. 执行Lua脚本限流
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(LIMIT_SCRIPT);
        redisScript.setResultType(Boolean.class);
        List<String> keys = Collections.singletonList(key);
        Boolean allowed = redisTemplate.execute(redisScript, keys, count, period);

        // 4. 限流判断
        if (Boolean.FALSE.equals(allowed)) {
            log.warn("【接口限流】key：{}，超出限流阈值：{}次/{}秒", key, count, period);
            throw new RuntimeException( "请求过于频繁，请稍后再试");
        }

        // 5. 放行请求
        return joinPoint.proceed();
    }

    /**
     * 生成限流key
     */
    private String generateKey(RateLimit rateLimit, ProceedingJoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            log.error("【限流切面】获取请求上下文失败，无法生成限流key");
            throw new RuntimeException("请求上下文异常，无法限流");
        }
        HttpServletRequest request = attributes.getRequest();
        String prefix = "rate:limit:";


        // 按IP限流
        if (rateLimit.limitType() == RateLimit.LimitType.IP) {
            String ip = getClientIp(request);
            return prefix + ip;
        }

        // 按用户ID限流（需登录）
        if (rateLimit.limitType() == RateLimit.LimitType.USER) {
            Long userId = UserContextHolder.getUserId();
            if (userId == null) {
                throw new RuntimeException( "请先登录");
            }
            return prefix  + userId;
        }

        return prefix;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "unknown";
    }
}