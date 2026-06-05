package com.zcy.forum.mapper.primary;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zcy.forum.domain.entity.Likes;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LikeMapper extends BaseMapper<Likes> {
}
