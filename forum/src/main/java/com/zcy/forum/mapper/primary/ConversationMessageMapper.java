package com.zcy.forum.mapper.primary;

import com.zcy.forum.domain.entity.ConversationMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 会话消息表（含系统通知） Mapper 接口
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-17
 */
@Mapper
public interface ConversationMessageMapper extends BaseMapper<ConversationMessage> {

}
