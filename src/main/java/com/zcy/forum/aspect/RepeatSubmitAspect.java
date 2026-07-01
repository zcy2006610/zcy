package com.zcy.forum.aspect;

import com.zcy.forum.annotation.RepeatSubmit;
import com.zcy.forum.utils.CacheUtil;
import com.zcy.forum.utils.SpElUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RepeatSubmitAspect {
    private final CacheUtil cacheUtil;
    private final SpElUtil spElUtil;

    @Around("@annotation(repeatSubmit)")
    public Object around(ProceedingJoinPoint point, RepeatSubmit repeatSubmit) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        String key = buildKey(point, repeatSubmit.key());

        // 校验重复提交
        if (cacheUtil.repeatCheck(key, repeatSubmit.expire())) {
            log.warn("检测到重复提交: {}", key);
            throw new RuntimeException(repeatSubmit.msg());
        }

        return point.proceed();
    }

    // 构建防重key
    private String buildKey(ProceedingJoinPoint point, String spEl) {
        String prefix = "repeat:submit:";
        if (spEl.isEmpty()) {
            // 无SpEL：使用类名+方法名
            return prefix + signatureToString(point);
        }
        // 有SpEL：解析参数
        return prefix + spElUtil.parseKey(spEl, point);
    }

    private String signatureToString(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        return signature.getDeclaringTypeName() + "." + signature.getMethod().getName();
    }
}