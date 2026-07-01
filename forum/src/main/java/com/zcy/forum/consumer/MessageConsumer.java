package com.zcy.forum.consumer;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zcy.forum.config.RabbitMqConfig;

import com.zcy.forum.constant.MessageConstant;
import com.zcy.forum.domain.entity.ConversationMessage;
import com.zcy.forum.domain.entity.UserConversation;
import com.zcy.forum.mapper.primary.ConversationMessageMapper;
import com.zcy.forum.mapper.primary.UserConversationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageConsumer {

    private final ConversationMessageMapper messageMapper;
    private final UserConversationMapper conversationMapper;

    private final StringRedisTemplate stringRedisTemplate;

    // ==========================
    // 消费消息（完整版）
    // ==========================
    @RabbitListener(queues = RabbitMqConfig.MESSAGE_QUEUE)
    public void handleMessage(String jsonMsg) {
        try {
            log.info("【MQ消费】消息：{}", jsonMsg);

            // 1. 反序列化
            ConversationMessage message = JSONUtil.toBean(jsonMsg, ConversationMessage.class);

            // ==============================================
            // 【1】插入消息表 → 消息持久化
            // ==============================================
            messageMapper.insert(message);
            log.info("【消息表】插入成功 ID:{}", message.getId());

            // ==============================================
            // 【2】更新消息表（例如：状态、已读、扩展字段）
            // ==============================================
            UpdateWrapper<ConversationMessage> msgUpdate = new UpdateWrapper<>();
            msgUpdate.lambda()
                    .eq(ConversationMessage::getId, message.getId()) // 消息ID
                    .set(ConversationMessage::getStatus, 0); // 示例：1=已同步到DB

            messageMapper.update(null, msgUpdate);
            log.info("【消息表】更新状态成功");

            // ==============================================
            // 【3】更新双方会话表（发送者 + 接收者）
            // ==============================================
            updateConversation(message);

            log.info("【MQ消费完成】消息+会话全部落地MySQL");

        } catch (Exception e) {
            log.error("【MQ消费失败】", e);
        }
    }

    // ==========================
    // 更新双方会话
    // ==========================
    private void updateConversation(ConversationMessage message) {
        Long senderId = message.getSenderId();
        Long receiverId = message.getReceiverId();
        String convId = message.getConversationId();
        Integer convType = message.getConvType();
        String lastContent = message.getContent();
        LocalDateTime now = LocalDateTime.now();

        // 发送者会话
        upsertConversation(senderId, receiverId, convId, convType, lastContent, now);
        // 接收者会话
        upsertConversation(receiverId, senderId, convId, convType, lastContent, now);
    }

    // ==========================
    // 会话不存在则创建，存在则更新
    // ==========================
    private void upsertConversation(Long userId, Long targetId, String convId,
                                    Integer convType, String lastContent, LocalDateTime updateTime) {

        LambdaQueryWrapper<UserConversation> query = new LambdaQueryWrapper<>();
        query.eq(UserConversation::getUserId, userId);
        query.eq(UserConversation::getConversationId, convId);

        UserConversation conv = conversationMapper.selectOne(query);

        if (conv != null) {
            // 更新
            UpdateWrapper<UserConversation> up = new UpdateWrapper<>();
            up.lambda()
                    .eq(UserConversation::getUserId, userId)
                    .eq(UserConversation::getConversationId, convId)
                    .set(UserConversation::getLastContent, lastContent)
                    .set(UserConversation::getUpdateTime, updateTime);
            conversationMapper.update(null, up);
        } else {
            // 新建
            UserConversation newConv = UserConversation.builder()
                    .userId(userId)
                    .targetId(targetId)
                    .conversationId(convId)
                    .convType(convType)
                    .lastContent(lastContent)
                    .isDeleted(0)
                    .updateTime(updateTime)
                    .build();
            conversationMapper.insert(newConv);
        }
    }
}