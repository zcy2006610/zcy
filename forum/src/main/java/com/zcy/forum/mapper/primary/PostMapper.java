package com.zcy.forum.mapper.primary;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zcy.forum.domain.entity.Posts;
import com.zcy.forum.domain.vo.PostsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface PostMapper extends BaseMapper<Posts> {



    @Select("""
        SELECT  id, user_id, category_id, title, slug, content, cover, excerpt, view_count,
        like_count, comment_count, collect_count, created_at
        FROM posts
        WHERE 
            (UNIX_TIMESTAMP(created_at) >#{lastTime} or (#{lastTime}=UNIX_TIMESTAMP(created_at) and #{lastId}>id))
            and #{categoryId}=category_id
        ORDER BY created_at ASC, id ASC
        LIMIT #{pageSize};
        """)
    List<Posts> selectByScroll(@Param("lastTime") Long lastTime, @Param("pageSize")
    Integer pageSize, @Param("categoryId") Long categoryId,@Param("lastId") Long lastId);

    @Select("""
        select category_id,count(category_id) as statics from posts where audit_status=1 group by category_id
    """)
    List<Map<String,Object>> countAll();

    @Select("""
        select count(category_id) from posts where audit_status=1 and category_id=#{id} 
        group by category_id
    """)
    Long countById(@Param("id") Long id);
}
