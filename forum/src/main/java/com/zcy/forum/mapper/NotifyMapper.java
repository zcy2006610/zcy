package com.zcy.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zcy.forum.domain.entity.Notifications;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotifyMapper extends BaseMapper<Notifications> {
}
