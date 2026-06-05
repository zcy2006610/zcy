package com.zcy.forum.aspect;

import com.zcy.forum.annotation.LocalLock;
import com.zcy.forum.manager.LocalLockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LocalLockAspect {

    private final LocalLockManager localLockManager;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.zcy.forum.annotation.LocalLock)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        LocalLock lockAnnotation = method.getAnnotation(LocalLock.class);

        // 1. 解析 SpEL 生成锁 key
        String lockKey = parseLockKey(lockAnnotation, point);
        ReentrantLock lock = localLockManager.getLock(lockKey);

        boolean lockSuccess = false;
        try {
            // 2. 尝试获取锁
            lockSuccess = lock.tryLock(lockAnnotation.waitTime(), lockAnnotation.unit());
            if (!lockSuccess) {
                throw new RuntimeException("请求频繁，请稍后再试");
            }
            log.info("本地锁加锁成功，key:{}", lockKey);

            // 3. 执行目标方法
            return point.proceed();
        } finally {
            // 4. 释放锁
            if (lockSuccess && lock.isHeldByCurrentThread()) {
                lock.unlock();
                localLockManager.removeLock(lockKey);
                log.info("本地锁释放成功，key:{}", lockKey);
            }
        }
    }

    /**
     * 解析 SpEL 表达式
     */
    private String parseLockKey(LocalLock lockAnnotation, ProceedingJoinPoint point) {
        String keyEl = lockAnnotation.key();
        String prefix = lockAnnotation.prefix();

        // 无表达式，使用方法全限定名作为key
        if (keyEl.isEmpty()) {
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            return prefix + method.getDeclaringClass().getName() + "." + method.getName();
        }

        // 解析 SpEL
        EvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = ((MethodSignature) point.getSignature()).getParameterNames();
        Object[] args = point.getArgs();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        Object value = parser.parseExpression(keyEl).getValue(context);
        return prefix + (value == null ? "" : value.toString());
    }
}