package com.zcy.forum.aspect;

import com.alibaba.fastjson.TypeReference;
import com.zcy.forum.annotation.CacheEvict;
import com.zcy.forum.annotation.CacheResult;
import com.zcy.forum.annotation.CacheUpdate;
import com.zcy.forum.utils.RedisCacheUtil;
import com.zcy.forum.utils.SpElUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {
    private final RedisCacheUtil redisCacheUtil;
    private final SpElUtil spElUtil;

    @Around("@annotation(cacheResult)")
    public Object cacheResult(ProceedingJoinPoint point, CacheResult cacheResult) throws Throwable {
        String key = cacheResult.prefix() + spElUtil.parseKey(cacheResult.key(), point);
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> returnType = method.getReturnType();

        // 1. 尝试从缓存获取
        Object cacheValue = getCacheValue(key, method);
        if (cacheValue != null) {
            log.info("【缓存命中】key:{}", key);
            return cacheValue;
        }

        // 2. 执行数据库查询
        Object dbValue = point.proceed();

        // 3. 回写缓存（null 也缓存，防穿透）
        if (dbValue != null) {
            redisCacheUtil.set(key, dbValue, cacheResult.expire(), cacheResult.unit());
        } else {
            redisCacheUtil.setNull(key, cacheResult.expire(), cacheResult.unit());
        }
        log.info("【缓存回写】key:{}", key);
        return dbValue;
    }

    // 核心：自动适配 普通对象 / List 泛型
    private Object getCacheValue(String key, Method method) {
        try {
            // 返回类型是集合/泛型
            if (List.class.isAssignableFrom(method.getReturnType()) ||
                    method.getGenericReturnType() != method.getReturnType()) {

                ParameterizedTypeReference<?> typeReference =
                        ParameterizedTypeReference.forType(method.getGenericReturnType());
                return redisCacheUtil.get(key, new TypeReference<Object>() {});
            } else {
                // 普通对象
                return redisCacheUtil.get(key, method.getReturnType());
            }
        } catch (Exception e) {
            log.error("缓存反序列化失败", e);
            return null;
        }
    }

    // ==================== 下面两个方法保持不变 ====================
    @Around("@annotation(cacheUpdate)")
    public Object cacheUpdate(ProceedingJoinPoint point, CacheUpdate cacheUpdate) throws Throwable {
        Object result = point.proceed();
        String key = cacheUpdate.prefix() + spElUtil.parseKey(cacheUpdate.key(), point);
        redisCacheUtil.set(key, result, cacheUpdate.expire(), cacheUpdate.unit());
        return result;
    }

    @Around("@annotation(cacheEvict)")
    public Object cacheEvict(ProceedingJoinPoint point, CacheEvict cacheEvict) throws Throwable {
        Object result = point.proceed();
        String key = cacheEvict.prefix() + spElUtil.parseKey(cacheEvict.key(), point);
        redisCacheUtil.delete(key);
        return result;
    }
}