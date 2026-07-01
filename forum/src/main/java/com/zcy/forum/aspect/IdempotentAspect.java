package com.zcy.forum.aspect;

import com.zcy.forum.annotation.Idempotent;
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
public class IdempotentAspect {
    private final CacheUtil cacheUtil;
    private final SpElUtil spElUtil;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint point, Idempotent idempotent) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        String key = buildKey(point, idempotent.key());

        // 幂等校验
        if (cacheUtil.repeatCheck(key, idempotent.expire())) {
            log.warn("检测到重复幂等请求: {}", key);
            throw new RuntimeException(idempotent.msg());
        }

        return point.proceed();
    }

    // 构建幂等key
    private String buildKey(ProceedingJoinPoint point, String spEl) {
        String prefix = "idempotent:";
        if (spEl.isEmpty()) {
            return prefix + signatureToString(point);
        }
        return prefix + spElUtil.parseKey(spEl, point);
    }

    private String signatureToString(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        return signature.getDeclaringTypeName() + "." + signature.getMethod().getName();
    }
}