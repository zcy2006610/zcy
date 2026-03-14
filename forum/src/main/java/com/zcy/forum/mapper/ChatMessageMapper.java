package com.zcy.forum.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zcy.forum.domain.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 聊天消息表 Mapper 接口
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-09
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {


}
