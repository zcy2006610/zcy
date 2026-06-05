package com.zcy.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.zcy.forum.config.RabbitMqConfig;
import com.zcy.forum.constant.MessageConstant;
import com.zcy.forum.domain.dto.ClearUnreadDTO;
import com.zcy.forum.domain.dto.DeleteConversationDTO;
import com.zcy.forum.domain.entity.ConversationMessage;
import com.zcy.forum.domain.entity.UserConversation;
import com.zcy.forum.domain.entity.Users;
import com.zcy.forum.domain.vo.UserConversationVO;

import com.zcy.forum.mapper.primary.ConversationMessageMapper;
import com.zcy.forum.mapper.primary.UserConversationMapper;
import com.zcy.forum.mapper.primary.UserMapper;
import com.zcy.forum.service.IUserConversationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcy.forum.utils.RabbitMqUtil;
import com.zcy.forum.utils.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户会话表 服务实现类
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-17
 */
@Service
public class UserConversationServiceImpl extends ServiceImpl<UserConversationMapper, UserConversation> implements IUserConversationService {

    @Autowired
    private UserConversationMapper convMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitMqUtil mqUtil;


    @Override
    public List<UserConversationVO> getConversationList(Long userId, Long lastSeq) {
        String key = MessageConstant.CONVERSATION_LIST_PREFIX + userId;

        // 1. 从 Redis ZSet 读取会话（按时间倒序，每次20条）
        Set<ZSetOperations.TypedTuple<String>> tupleSet = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(
                        key,
                        0,
                        lastSeq > 0 ? lastSeq : System.currentTimeMillis(),
                        0,
                        20
                );

        if (tupleSet == null || tupleSet.isEmpty()) {
            // 这里可以做兜底：Redis没有 -> 查DB（可选）
            return new ArrayList<>();
        }

        // 2. 反序列化 JSON -> UserConversation -> UserConversationVO
        List<UserConversationVO> result = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> tuple : tupleSet) {

            String json = tuple.getValue();
            UserConversation conv = JSONUtil.toBean(json, UserConversation.class);
            if (conv.getIsDeleted() == 1) {
                continue;
            }
            // VO 转换
            UserConversationVO vo = new UserConversationVO();
            vo.setConversationId(conv.getConversationId());
            vo.setUserId(conv.getUserId());
            vo.setTargetId(conv.getTargetId());
            vo.setConvType(conv.getConvType());
            vo.setLastContent(conv.getLastContent());
            vo.setUpdateTime(conv.getUpdateTime());

            // 放入未读数量（从Redis读取）
            String unreadKey = MessageConstant.CONVERSATION_UNREAD_PREFIX + userId + ":" + conv.getConversationId();
            String unreadStr = stringRedisTemplate.opsForValue().get(unreadKey);
            vo.setUnreadCount(unreadStr == null ? 0 : Integer.parseInt(unreadStr));

            result.add(vo);
        }

        return result;
    }

    @Override
    public void clearUnread(Long userId, String conversationId) {
        // ======================
        // 1. Redis 未读清零（同步）
        // ======================
        String unreadKey = MessageConstant.CONVERSATION_UNREAD_PREFIX + userId + ":" + conversationId;
        stringRedisTemplate.opsForValue().set(unreadKey, "0");

        // ======================
        // 2. 发送 MQ 异步消息
        // ======================
        // 构造消息体
        ClearUnreadDTO dto = new ClearUnreadDTO();
        dto.setUserId(userId);
        dto.setConversationId(conversationId);

        // 发送到 MQ
        mqUtil.sendMsg(RabbitMqConfig.CLEAR_UNREAD_KEY, JSONUtil.toJsonStr(dto));

    }

    @Override
    public void deleteConversation(Long userId, String conversationId) {
        // ==============================
        // 【重要】Redis 不删除任何数据！
        // 只发送异步消息 -> 消费者软删除 DB
        // ==============================

        DeleteConversationDTO dto = new DeleteConversationDTO();
        dto.setUserId(userId);
        dto.setConversationId(conversationId);

        // 发送 MQ
        mqUtil.sendMsg(RabbitMqConfig.DELETE_CONVERSATION_KEY, JSONUtil.toJsonStr(dto));
    }




}
