package com.zcy.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置类
 */
@Configuration
public class PasswordConfig {

    /**
     * 注入BCrypt密码加密器（默认强度10）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 可选：指定加密强度（如12，建议10-12）
        // return new BCryptPasswordEncoder(12);
        return new BCryptPasswordEncoder();
    }
}