package com.zcy.forum.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    /**
     * 限流key（默认取接口路径）
     */
    String key() default "";

    /**
     * 限流时间窗口（秒）
     */
    int period() default 60;

    /**
     * 时间窗口内最大请求次数
     */
    int count() default 10;

    /**
     * 限流维度（IP/USER，默认IP）
     */
    LimitType limitType() default LimitType.USER;

    /**
     * 限流维度枚举
     */
    enum LimitType {
        IP, // 按IP限流
        USER // 按登录用户ID限流
    }
}