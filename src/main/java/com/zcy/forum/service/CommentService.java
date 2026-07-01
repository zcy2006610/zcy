package com.zcy.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zcy.forum.common.PageResult;
import com.zcy.forum.domain.dto.CommentPublishDTO;
import com.zcy.forum.domain.entity.Comments;
import com.zcy.forum.domain.vo.CommentsVo;
import org.springframework.stereotype.Service;


public interface CommentService extends IService<Comments> {
    Long publishComment(CommentPublishDTO commentDTO);

    PageResult<CommentsVo> getFirstLevelComments(Long postId, Integer pageNum, Integer pageSize);

    PageResult<CommentsVo> getChildComments(Long parentId, Integer pageNum, Integer pageSize);

    void deleteComment(Long id);

    void likeComment(Long id);

    PageResult<CommentsVo> getMyComments(Integer pageNum, Integer pageSize);
}
