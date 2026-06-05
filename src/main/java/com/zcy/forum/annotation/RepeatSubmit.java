package com.zcy.forum.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatSubmit {
    /**
     * 防重时间（秒）默认2秒
     */
    int expire() default 2;

    /**
     * SpEL表达式key（如 #userId #orderId）
     */
    String key() default "";

    /**
     * 提示语
     */
    String msg() default "请求频繁，请稍后再试";
}