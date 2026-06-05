package com.zcy.forum.annotation;

import java.lang.annotation.*;

/**
 * 自定义注解：标记该方法需要登录才能访问
 */
// 注解作用在方法上
@Target(ElementType.METHOD)
// 注解会保留到运行时，拦截器能解析
@Retention(RetentionPolicy.RUNTIME)
// 生成文档时显示该注解
@Documented
public @interface RequireLogin {
    // 注解可加参数（可选），比如是否强制校验，默认true
    boolean required() default true;
}