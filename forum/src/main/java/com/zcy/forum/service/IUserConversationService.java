package com.zcy.forum.service;

import com.zcy.forum.domain.entity.UserConversation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zcy.forum.domain.vo.UserConversationVO;

import java.util.List;

/**
 * <p>
 * 用户会话表 服务类
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-17
 */
public interface IUserConversationService extends IService<UserConversation> {

    List<UserConversationVO> getConversationList(Long userId);

    void clearUnread(Long userId, String conversationId);

    void deleteConversation(Long userId, String conversationId);
}
