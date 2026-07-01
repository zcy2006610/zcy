package com.zcy.forum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许所有路径跨域
                .allowedOriginPatterns("*") // 允许所有域名（生产环境指定具体域名）
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 允许的请求方法
                .allowedHeaders("*") // 允许所有请求头（关键：放行Authorization）
                .allowCredentials(true) // 允许携带cookie（如果需要）
                .maxAge(3600); // 预检请求缓存时间
    }
}