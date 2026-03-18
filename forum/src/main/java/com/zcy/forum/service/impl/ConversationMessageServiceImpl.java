package com.zcy.forum.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zcy.forum.constant.MessageConstant;
import com.zcy.forum.domain.dto.UserMessageDTO;
import com.zcy.forum.domain.entity.ConversationMessage;
import com.zcy.forum.domain.vo.ConversationMessageVO;
import com.zcy.forum.domain.vo.UserConversationVO;

import com.zcy.forum.mapper.primary.ConversationMessageMapper;
import com.zcy.forum.service.IConversationMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcy.forum.utils.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 会话消息表（含系统通知） 服务实现类
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-17
 */
@Service
public class ConversationMessageServiceImpl extends ServiceImpl<ConversationMessageMapper, ConversationMessage> implements IConversationMessageService {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<UserMessageDTO> pullNewMessage(String conversationId, Long lastTime) {
        Long userId = UserContextHolder.getUserId();
        String key = MessageConstant.SINGLE_MSG_LIST_PREFIX +userId+":"+ conversationId;

        // 只拉 > lastMsgTime 的消息
        Set<String> set = stringRedisTemplate.opsForZSet().rangeByScore(
                key,
                lastTime + 1,
                System.currentTimeMillis(),
                0,
                100 // 足够大，一次拉完所有新消息
        );

        if (CollUtil.isEmpty(set)) return new ArrayList<>();

        return set.stream()
                .map(json -> JSONUtil.toBean(json, UserMessageDTO.class))
                .toList();
    }

    @Override
    public List<UserMessageDTO> pullHistoryMessage(String conversationId, Long lastMsgTime, Integer size) {
        Long userId = UserContextHolder.getUserId();
        String key = MessageConstant.SINGLE_MSG_LIST_PREFIX +userId+":"+ conversationId;

        long max = lastMsgTime > 0 ? lastMsgTime - 1 : System.currentTimeMillis();

        // 倒序取历史，固定条数，offset 永远 0
        Set<String> set = stringRedisTemplate.opsForZSet().reverseRangeByScore(
                key,
                0,          // 最小时间
                max,        // 最大时间（上一条-1）
                0,          // 偏移永远 0
                size        // 固定条数
        );

        if (CollUtil.isEmpty(set)) return new ArrayList<>();

        // 反转成正常时间顺序
        List<String> list = new ArrayList<>(set);
        Collections.reverse(list);

        return list.stream()
                .map(json -> JSONUtil.toBean(json, UserMessageDTO.class))
                .toList();
    }

    @Override
    public void readMessage(String msgId) {
        stringRedisTemplate.opsForValue().set("msg:status:"+UserContextHolder.getUserId()+":"+msgId,"1");
    }


}




















