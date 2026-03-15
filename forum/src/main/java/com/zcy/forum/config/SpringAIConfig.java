package com.zcy.forum.config;

import com.zcy.forum.tool.PostTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SpringAIConfig {
    /**
     * 配置 ChatClient
     */
    @Bean
    @Primary
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
                                 PostTool postTool
    ) {  // 日志记录器
        return chatClientBuilder
                .defaultTools(postTool) //添加默认工具
                .build();
    }
}
