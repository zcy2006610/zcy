package com.zcy.forum.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisCacheUtil {
    private final StringRedisTemplate stringRedisTemplate;

    // 1. 普通对象反序列化
    public <T> T get(String key, Class<T> clazz) {
        String value = stringRedisTemplate.opsForValue().get(key);
        return value == null ? null : JSON.parseObject(value, clazz);
    }

    // 2. 支持泛型集合/复杂类型（核心修复）
    public <T> T get(String key, TypeReference<T> typeReference) {
        String value = stringRedisTemplate.opsForValue().get(key);
        return value == null ? null : JSON.parseObject(value, typeReference);
    }

    // 3. 通用 set
    public void set(String key, Object value, long expire, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(value), expire, unit);
    }

    // 4. 删除
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    // 5. 缓存空值（防穿透）
    public void setNull(String key, long expire, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, "null", expire, unit);
    }
}