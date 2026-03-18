package com.zcy.forum.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    /**
     * 幂等保持时间（秒）默认10分钟
     */
    int expire() default 600;

    /**
     * SpEL表达式key
     */
    String key() default "";

    /**
     * 提示语
     */
    String msg() default "重复请求，接口已幂等处理";
}