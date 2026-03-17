package com.zcy.forum.utils;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * Redis 管理用户在线状态
 */
@Component
public class OnlineStatusUtils {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // Redis Key 前缀
    private static final String ONLINE_KEY_PREFIX = "user:online:";
    // 过期时间：心跳兜底
    private static final long EXPIRE_TIME = 5;

    /**
     * 用户上线：保存用户ID与WebSocket会话ID
     */
    public void userOnline(Long userId, String sessionId) {
        stringRedisTemplate.opsForValue().set(ONLINE_KEY_PREFIX + userId, sessionId, EXPIRE_TIME, TimeUnit.MINUTES);
    }

    /**
     * 用户下线：删除在线状态
     */
    public void userOffline(Long userId) {
        stringRedisTemplate.delete(ONLINE_KEY_PREFIX + userId);
    }

    /**
     * 判断用户是否在线
     */
    public boolean isOnline(Long userId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(ONLINE_KEY_PREFIX + userId));
    }

    /**
     * 获取用户的WebSocket会话ID
     */
    public String getSessionId(Long userId) {
        return stringRedisTemplate.opsForValue().get(ONLINE_KEY_PREFIX + userId);
    }
}