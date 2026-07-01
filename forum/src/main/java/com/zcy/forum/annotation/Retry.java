package com.zcy.forum.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retry {
    /**
     * 最大重试次数 默认3次
     */
    int retryCount() default 3;

    /**
     * 每次重试间隔毫秒 默认100ms
     */
    long delay() default 100;

    /**
     * 哪些异常需要重试
     */
    Class<? extends Throwable>[] include() default {Exception.class};
}