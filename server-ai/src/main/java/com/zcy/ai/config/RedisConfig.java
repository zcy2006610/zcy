package com.zcy.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 正确的Redis配置类（包含StringRedisTemplate和RedisTemplate）
 */
@Configuration
public class RedisConfig {

    /**
     * 配置StringRedisTemplate（切面中使用的是这个，优先保证它正确）
     * Spring默认也会创建，但自定义可统一序列化规则
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        // 核心：设置RedisConnectionFactory（解决"RedisConnectionFactory is required"异常）
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    /**
     * 配置通用的RedisTemplate（可选，用于存储对象）
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 核心：绑定RedisConnectionFactory
        template.setConnectionFactory(redisConnectionFactory);

        // 设置序列化规则（可选，避免乱码）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // key和hashKey使用String序列化
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        // value和hashValue使用JSON序列化
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // 初始化模板
        template.afterPropertiesSet();
        return template;
    }
}