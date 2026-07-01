package com.zcy.forum.mapper.primary;

import com.zcy.forum.domain.entity.SearchHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户搜索历史表 Mapper 接口
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-15
 */
@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

}
