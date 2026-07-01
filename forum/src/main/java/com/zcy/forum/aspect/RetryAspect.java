package com.zcy.forum.aspect;

import com.zcy.forum.annotation.Retry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class RetryAspect {

    @SneakyThrows
    @Around("@annotation(retry)")
    public Object around(ProceedingJoinPoint point, Retry retry) {
        int maxRetry = retry.retryCount();
        long delay = retry.delay();
        Class<? extends Throwable>[] includeExceptions = retry.include();

        Throwable exception = null;
        // 重试逻辑：1次正常执行 + N次重试
        for (int i = 0; i <= maxRetry; i++) {
            try {
                // 执行业务方法
                return point.proceed();
            } catch (Throwable e) {
                exception = e;
                // 判断是否需要重试
                if (!matchException(e, includeExceptions)) {
                    throw e;
                }
                // 最后一次仍失败，抛出异常
                if (i == maxRetry) {
                    log.error("方法重试{}次后彻底失败", maxRetry);
                    throw e;
                }
                // 重试间隔
                log.warn("第{}次执行失败，{}ms后重试，异常：{}", i + 1, delay, e.getMessage());
                Thread.sleep(delay);
            }
        }
        throw exception;
    }

    // 匹配异常类型
    private boolean matchException(Throwable e, Class<? extends Throwable>[] include) {
        for (Class<? extends Throwable> clazz : include) {
            if (clazz.isInstance(e)) {
                return true;
            }
        }
        return false;
    }
}