package com.zcy.forum.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CacheUtil {
    // 单机本地缓存
    private final ConcurrentHashMap<String, Long> LOCAL_CACHE = new ConcurrentHashMap<>();
    // 分布式Redis缓存
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * true=重复请求  false=首次请求
     */
    public boolean repeatCheck(String key, int expireSeconds) {
        // ======================
        // 分布式环境（推荐生产）
        // ======================
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", expireSeconds, TimeUnit.SECONDS);
        return Boolean.FALSE.equals(success);

        // ======================
        // 单机环境（本地测试）
        // ======================
        // if (LOCAL_CACHE.containsKey(key)) return true;
        // LOCAL_CACHE.put(key, System.currentTimeMillis());
        // new Timer().schedule(new TimerTask() {
        //     @Override
        //     public void run() { LOCAL_CACHE.remove(key); }
        // }, expireSeconds * 1000L);
        // return false;
    }
}