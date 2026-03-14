package com.zcy.forum.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 3.5.15 分页插件配置类
 * 适配 MP 3.5.15 版本，解决爆红问题
 */
@Configuration
public class MybatisConfig {

    /**
     * 配置分页插件（核心）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 初始化分页插件，指定数据库类型（MySQL）
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);

        // 2. 分页插件可选配置（MP 3.5.15 完全支持）
        paginationInterceptor.setMaxLimit(30L); // 单页最大条数限制（防止一次性查过多数据）
        paginationInterceptor.setOverflow(true); // 页码超出总页数时，自动查询最后一页（false 则返回空列表）
        paginationInterceptor.setDbType(DbType.MYSQL); // 显式指定数据库类型（增强兼容性）

        // 3. 将分页插件添加到拦截器链（MP 3.5.x 核心写法）
        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }
}