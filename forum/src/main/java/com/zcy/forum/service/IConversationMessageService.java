package com.zcy.forum.service;

import com.zcy.forum.domain.dto.UserMessageDTO;
import com.zcy.forum.domain.entity.ConversationMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zcy.forum.domain.vo.ConversationMessageVO;
import com.zcy.forum.domain.vo.UserConversationVO;

import java.util.List;

/**
 * <p>
 * 会话消息表（含系统通知） 服务类
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-17
 */
public interface IConversationMessageService extends IService<ConversationMessage> {


    List<UserMessageDTO> pullNewMessage(String conversationId, Long lastTime);

    List<UserMessageDTO> pullHistoryMessage(String conversationId, Long lastMsgTime, Integer size);

    void readMessage(String msgId);
}
