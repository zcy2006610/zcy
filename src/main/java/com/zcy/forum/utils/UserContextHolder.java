package com.zcy.forum.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户上下文工具类（基于ThreadLocal存储当前登录用户信息）
 * 注意：必须在请求结束时调用remove()清空，避免内存泄漏
 */
@Slf4j
public class UserContextHolder {
    // 定义ThreadLocal，存储当前登录用户信息
    private static final ThreadLocal<Long> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 存入用户信息
     */
    public static void setUserId(Long userId) {
        if (userId == null) {
            log.warn("【UserContextHolder】存入的用户信息为null");
            return;
        }
        USER_THREAD_LOCAL.set(userId);
        log.debug("【UserContextHolder】线程{}存入用户信息：{}", Thread.currentThread().getId(), userId);
    }

    /**
     * 获取当前登录用户信息
     */
    public static Long getUserId() {
        Long userId = USER_THREAD_LOCAL.get();
        if (userId == null) {
            log.warn("【UserContextHolder】线程{}未获取到用户信息（可能未登录）", Thread.currentThread().getId());
            return null;
        }
        return userId;
    }

    /**
     * 清空当前线程的用户信息（必须调用！）
     */
    public static void remove() {
        log.debug("【UserContextHolder】线程{}清空用户信息", Thread.currentThread().getId());
        USER_THREAD_LOCAL.remove();
    }

}