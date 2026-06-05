package com.zcy.forum.consumer;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zcy.forum.config.RabbitMqConfig;
import com.zcy.forum.constant.MessageConstant;
import com.zcy.forum.domain.dto.DeleteConversationDTO;
import com.zcy.forum.domain.entity.UserConversation;
import com.zcy.forum.mapper.primary.UserConversationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DeleteConversationConsumer {

    private final UserConversationMapper conversationMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = RabbitMqConfig.DELETE_CONVERSATION_QUEUE)
    public void handleDelete(String message) {
        DeleteConversationDTO dto = JSONUtil.toBean(message, DeleteConversationDTO.class);
        Long userId = dto.getUserId();
        String convId = dto.getConversationId();

        // ==========================================
        // 1. 数据库软删除
        // ==========================================
        LambdaUpdateWrapper<UserConversation> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserConversation::getUserId, userId)
               .eq(UserConversation::getConversationId, convId);

        UserConversation update = UserConversation.builder().build();
        update.setIsDeleted(1);
        conversationMapper.update(update, wrapper);

        // ==========================================
        // 2. 更新 Redis 里的会话 JSON，标记 isDeleted=1
        // ==========================================
        String convKey = MessageConstant.CONVERSATION_LIST_PREFIX + userId;

        // ① 把 ZSet 里这条会话查出来
        Set<String> members = stringRedisTemplate.opsForZSet().range(convKey, 0, -1);
        if (members == null) return;

        for (String json : members) {
            UserConversation conv = JSONUtil.toBean(json, UserConversation.class);
            if (conv.getConversationId().equals(convId)) {
                // ② 标记删除
                conv.setIsDeleted(1);
                String newJson = JSONUtil.toJsonStr(conv);

                // ③ 先删旧的，再加新的（ZSet 只能这样更新）
                stringRedisTemplate.opsForZSet().remove(convKey, json);
                stringRedisTemplate.opsForZSet().add(convKey, newJson, System.currentTimeMillis());
                break;
            }
        }
    }
}