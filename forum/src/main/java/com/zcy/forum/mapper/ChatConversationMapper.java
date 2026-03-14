package com.zcy.forum.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zcy.forum.domain.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 聊天会话表 Mapper 接口
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-09
 */
@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {

}
