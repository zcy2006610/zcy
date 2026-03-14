package com.zcy.forum.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类：队列、交换机、绑定
 */
@Configuration
public class RabbitMqConfig {

    // ====================== 队列名称 ======================
    public static final String POST_QUEUE_V1 = "post.queue.version1";
    public static final String POST_QUEUE_V2 = "post.queue.version2";
    public static final String COMMENT_QUEUE = "comment.queue";

    // ====================== 交换机 ======================
    public static final String EXCHANGE = "forum.exchange";

    // ====================== 路由键 ======================
    public static final String POST_ROUTING_KEY_V1 = "post.routing.key1";
    public static final String POST_ROUTING_KEY_V2 = "post.routing.key2";
    public static final String COMMENT_ROUTING_KEY = "comment.routing.key";

    // ---------------------- 队列 ----------------------
    @Bean
    public Queue postQueue1() {
        return new Queue(POST_QUEUE_V1, true); // 持久化队列
    }

    @Bean
    public Queue postQueue2() {
        return new Queue(POST_QUEUE_V2, true); // 持久化队列
    }
    @Bean
    public Queue commentQueue() {
        return new Queue(COMMENT_QUEUE, true);
    }

    // ---------------------- 交换机 ----------------------
    @Bean
    public Exchange forumExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    // ---------------------- 绑定 ----------------------
    @Bean
    public Binding postBinding1() {
        return BindingBuilder.bind(postQueue1())
                .to(forumExchange())
                .with(POST_ROUTING_KEY_V1)
                .noargs();
    }

    @Bean
    public Binding commentBinding() {
        return BindingBuilder.bind(commentQueue())
                .to(forumExchange())
                .with(COMMENT_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding postBinding2() {
        return BindingBuilder.bind(postQueue2())
                .to(forumExchange())
                .with(POST_ROUTING_KEY_V2)
                .noargs();
    }
}