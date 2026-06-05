package com.zcy.forum.mapper.primary;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zcy.forum.domain.entity.SensitiveWords;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWords> {
}
