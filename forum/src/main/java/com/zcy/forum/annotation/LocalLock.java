package com.zcy.forum.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 本地方法锁注解（单机版）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LocalLock {

    /**
     * 锁的key（支持SpEL表达式）
     * 示例：key = "#userId" 、key = "#order.id" 、key = "'global:lock'"
     */
    String key() default "";

    /**
     * 等待获取锁的超时时间
     */
    long waitTime() default 3;

    /**
     * 时间单位
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 锁前缀（区分业务）
     */
    String prefix() default "local:lock:";
}