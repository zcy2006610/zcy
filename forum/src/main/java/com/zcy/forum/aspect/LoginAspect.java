package com.zcy.forum.aspect;

import com.zcy.forum.annotation.RequireLogin;
import com.zcy.forum.constant.LoginConstant;
import com.zcy.forum.utils.JwtTokenUtil;
import com.zcy.forum.utils.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * 登录校验切面：拦截@RequireLogin注解的方法，校验登录状态
 */
@Aspect // 标记为切面类
@Component // 交给Spring管理
@Slf4j
public class LoginAspect {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;
    // 1. 定义切点：匹配所有加了@RequireLogin注解的方法
    @Pointcut("@annotation(com.zcy.forum.annotation.RequireLogin)")
    public void loginPointcut() {}

    // 2. 环绕通知：方法执行前校验登录，执行后返回结果
    @Around("loginPointcut()")
    public Object aroundLoginCheck(ProceedingJoinPoint joinPoint) throws Throwable {
        // ========== 1. 获取请求上下文（用于取Token/Session） ==========
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return buildUnLoginResponse(); // 无请求上下文，返回未登录
        }
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        // ========== 2. 解析注解（可选，可获取注解参数） ==========
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireLogin requireLogin = method.getAnnotation(RequireLogin.class);
        if (!requireLogin.required()) {
            return joinPoint.proceed(); // 注解指定无需强制登录，直接执行方法
        }

        // ========== 3. 校验登录状态（核心：替换成你的真实逻辑） ==========
        boolean isLogin = checkLoginStatus(request);
        if (!isLogin) {
            // 未登录：返回JSON提示，不执行原方法
            return buildUnLoginResponse(response);
        }

        // ========== 4. 已登录：执行原方法并返回结果 ==========
        return joinPoint.proceed();
    }

    /**
     * 校验登录状态（核心：替换成你项目的登录校验逻辑）
     * 示例：从Header取JWT Token，验证有效性 + 检查Redis黑名单
     */
    private boolean checkLoginStatus(HttpServletRequest request) {
        try {
            // 1. 从请求头获取Token（和前端约定：Header的Authorization字段）
            String authHeader = request.getHeader("Authorization");

            String token = jwtTokenUtil.extractTokenFromHeader(authHeader);
            if (token == null || token.isEmpty()) {
                log.warn("请求头中未包含有效的JWT令牌");
                return false;
            }

            // 3. 校验Token有效性（第二步校验：签名、过期时间等）
            if (!jwtTokenUtil.validateToken(token)) {
                log.warn("JWT令牌无效或已过期");
                return false;
            }

            // 4. 安全获取用户ID（第三步：提取并转换userId）
            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            if (userId == null) {
                log.warn("JWT令牌中未包含有效的用户ID");
                return false;
            }
            String redisToken = redisTemplate.opsForValue().get(LoginConstant.USER_LOGIN_KEY + userId);
            if(!StringUtils.hasText(redisToken)){
                log.info("redis中的jwt令牌已失效");
                return false;
            }
            UserContextHolder.setUserId(userId);
            return true;
        } catch (Exception e) {
            log.error("登录状态校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 构建未登录的返回结果
     */
    private Object buildUnLoginResponse(HttpServletResponse response) {
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401); // 响应状态码：401未授权
            PrintWriter writer = response.getWriter();
            // 返回和前端约定的JSON格式
            writer.write("{\"code\":401,\"msg\":\"请先登录后再操作\",\"data\":null}");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 无请求上下文时的兜底返回
    private Object buildUnLoginResponse() {
        return "{\"code\":401,\"msg\":\"请先登录后再操作\",\"data\":null}";
    }
}