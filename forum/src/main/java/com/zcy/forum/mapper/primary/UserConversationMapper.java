package com.zcy.forum.mapper.primary;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zcy.forum.domain.entity.UserConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户会话表 Mapper 接口
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-17
 */
@Mapper
public interface UserConversationMapper extends BaseMapper<UserConversation> {

}
