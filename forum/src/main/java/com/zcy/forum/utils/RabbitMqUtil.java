package com.zcy.forum.utils;

import com.zcy.forum.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 工具类：发送消息
 */
@Component
public class RabbitMqUtil {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param msg 消息内容
     */
    public void sendMsg(String exchange, String routingKey, Object msg) {
        rabbitTemplate.convertAndSend(exchange, routingKey, msg);
    }

    // ==================== 简化发送（你的项目直接用） ====================
    public void sendPostMsg(Object msg) {
        sendMsg(RabbitMqConfig.EXCHANGE, RabbitMqConfig.POST_ROUTING_KEY_V1, msg);
    }

    public void sendMsg(String routingKey,Object msg){
        sendMsg(RabbitMqConfig.EXCHANGE, routingKey, msg);
    }
    public void sendCommentMsg(Object msg) {
        sendMsg(RabbitMqConfig.EXCHANGE, RabbitMqConfig.COMMENT_ROUTING_KEY, msg);
    }
}