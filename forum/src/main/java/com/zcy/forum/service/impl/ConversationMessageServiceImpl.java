package com.zcy.forum.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zcy.forum.domain.entity.ConversationMessage;
import com.zcy.forum.domain.vo.ConversationMessageVO;
import com.zcy.forum.domain.vo.UserConversationVO;

import com.zcy.forum.mapper.primary.ConversationMessageMapper;
import com.zcy.forum.service.IConversationMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcy.forum.utils.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
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



    @Override
    public List<ConversationMessage> pullNewMessage(String conversationId, Long lastSeq) {
        if(conversationId==null||lastSeq==null) {
            return Collections.emptyList();
        }
        List<ConversationMessage> messageList = lambdaQuery().eq(ConversationMessage::getConversationId, conversationId)
                .eq(ConversationMessage::getStatus,0)
                .eq(ConversationMessage::getConvType,1)
                .gt(ConversationMessage::getSeq, lastSeq).list();
        if(CollectionUtil.isEmpty(messageList)) {
            ConversationMessage vo = new ConversationMessage();
            vo.setContent("没有更多消息了");
            return List.of(vo);
        }

    }
}











}
