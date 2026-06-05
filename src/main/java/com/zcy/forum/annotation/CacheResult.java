package com.zcy.forum.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheResult {
    /**
     * 缓存key (支持SpEL)
     */
    String key();

    /**
     * 过期时间
     */
    long expire() default 300;

    /**
     * 时间单位
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 缓存前缀
     */
    String prefix() default "cache:";
}