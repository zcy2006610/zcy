package com.zcy.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zcy.forum.common.PageResult;
import com.zcy.forum.domain.dto.Post2DraftDTO;
import com.zcy.forum.domain.dto.PostEditDTO;
import com.zcy.forum.domain.dto.PostPublishDTO;
import com.zcy.forum.domain.dto.PostUpdateDTO;
import com.zcy.forum.domain.entity.Posts;
import com.zcy.forum.domain.vo.PostDetailVo;
import com.zcy.forum.domain.vo.PostDraftsVo;
import com.zcy.forum.domain.vo.PostResponseVo;
import com.zcy.forum.domain.vo.PostsVo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PostService extends IService<Posts> {


    PostDetailVo detail(Long id);

    Long publish(PostPublishDTO publishDTO);

    void updatePost(PostUpdateDTO updateDTO);

    PostUpdateDTO trace(Long id);

    void logicDelete(Long id);

    List<PostsVo> queryMyPost(Long userId);

    void editMyPost(PostEditDTO editDTO);

    Long save2draft(Post2DraftDTO draftDTO);

    PageResult<PostsVo> getPage(Integer pageNum, Integer pageSize,Map<String,Object> params);

    PostDraftsVo getDraft(Long id);
}
