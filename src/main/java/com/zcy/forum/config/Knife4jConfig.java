package com.zcy.forum.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableKnife4j
public class Knife4jConfig {

    /**
     * 配置接口文档基本信息
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // 文档标题
                .info(new Info()
                        .title("论坛项目接口文档")
                        // 文档描述
                        .description("论坛项目的用户、帖子、评论、权限等核心接口")
                        // 版本号
                        .version("1.0.0")
                        // 联系人信息（可选）
                        .contact(new Contact()
                                .name("张城逸")
                                .email("3326543986@qq.com")));
    }
}