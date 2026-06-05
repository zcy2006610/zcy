package com.zcy.forum.annotation;


import java.lang.annotation.*;

/**
 * 自定义注解：标记该方法需要特定权限才能访问
 * 权限校验逻辑：普通用户（role=0）无法访问，其他角色可以访问
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    boolean required() default true;
}
