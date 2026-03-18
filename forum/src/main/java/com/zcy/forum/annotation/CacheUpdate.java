package com.zcy.forum.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheUpdate {
    String key();
    long expire() default 300;
    TimeUnit unit() default TimeUnit.SECONDS;
    String prefix() default "cache:";
}