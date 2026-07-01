package com.zcy.forum.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        context = applicationContext;
    }

    // 根据类型获取 Bean
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    // 根据名字获取 Bean
    public static Object getBean(String name) {
        return context.getBean(name);
    }
}