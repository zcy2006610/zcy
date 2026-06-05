package com.zcy.forum.manager;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 本地锁管理器：缓存所有锁实例
 */
@Component
public class LocalLockManager {

    // 线程安全的Map，存储锁对象
    private final Map<String, ReentrantLock> LOCK_MAP = new ConcurrentHashMap<>();

    /**
     * 获取锁
     */
    public ReentrantLock getLock(String lockKey) {
        // 不存在则创建，存在则复用
        return LOCK_MAP.computeIfAbsent(lockKey, k -> new ReentrantLock());
    }

    /**
     * 移除锁（避免内存溢出，可根据业务开启）
     */
    public void removeLock(String lockKey) {
        ReentrantLock lock = LOCK_MAP.get(lockKey);
        if (lock != null && !lock.hasQueuedThreads()) {
            LOCK_MAP.remove(lockKey);
        }
    }
}