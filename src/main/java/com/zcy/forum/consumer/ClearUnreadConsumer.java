package com.zcy.forum.consumer;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zcy.forum.config.RabbitMqConfig;
import com.zcy.forum.domain.dto.ClearUnreadDTO;
import com.zcy.forum.domain.entity.UserConversation;
import com.zcy.forum.mapper.primary.UserConversationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClearUnreadConsumer {

    private final UserConversationMapper conversationMapper;

    @RabbitListener(queues = RabbitMqConfig.CLEAR_UNREAD_QUEUE)
    public void handleClearUnread(String message) {
        try {
            ClearUnreadDTO dto = JSONUtil.toBean(message, ClearUnreadDTO.class);
            Long userId = dto.getUserId();
            String conversationId = dto.getConversationId();

            // ======================
            // DB 更新未读 = 0
            // ======================
            LambdaUpdateWrapper<UserConversation> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(UserConversation::getUserId, userId)
                    .eq(UserConversation::getConversationId, conversationId);

            UserConversation update = UserConversation.builder().build();
            update.setUnreadCount(0); // 未读清零

            conversationMapper.update(update, wrapper);

        } catch (Exception e) {
            // 异常处理
        }
    }
}