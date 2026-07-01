package com.zcy.forum.controller;

import com.zcy.forum.annotation.RequireLogin;
import com.zcy.forum.common.PageResult;
import com.zcy.forum.common.Result;
import com.zcy.forum.domain.dto.CommentPublishDTO;
import com.zcy.forum.domain.vo.CommentsVo;
import com.zcy.forum.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@Tag(name = "评论模块")
@Slf4j
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/publish")
    @Operation(summary = "发布评论")
    @RequireLogin
    public Result<Long> publishComment(@RequestBody @Validated CommentPublishDTO commentDTO) {
        try {
            Long commentId = commentService.publishComment(commentDTO);
            return Result.ok(commentId);
        } catch (Exception e) {
            log.error("发布评论失败: {}", e.getMessage(), e);
            return Result.fail("发布评论失败: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/firstLevel/{postId}")
    @Operation(summary = "分页查询某帖子一级评论")
    public Result<PageResult<CommentsVo>> getFirstLevelComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            PageResult<CommentsVo> result = commentService.getFirstLevelComments(postId, pageNum, pageSize);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询一级评论失败: {}", e.getMessage(), e);
            return Result.fail("查询一级评论失败: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/children/{parentId}")
    @Operation(summary = "查询某条评论子评论")
    public Result<PageResult<CommentsVo>> getChildComments(
            @PathVariable Long parentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            PageResult<CommentsVo> result = commentService.getChildComments(parentId, pageNum, pageSize);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询子评论失败: {}", e.getMessage(), e);
            return Result.fail("查询子评论失败: " + e.getMessage(), 500);
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除评论")
    @RequireLogin
    public Result<String> deleteComment(@PathVariable Long id) {
        try {
            commentService.deleteComment(id);
            return Result.ok("删除评论成功");
        } catch (Exception e) {
            log.error("删除评论失败: {}", e.getMessage(), e);
            return Result.fail("删除评论失败: " + e.getMessage(), 500);
        }
    }

    @PostMapping("/like/{id}")
    @Operation(summary = "评论点赞")
    @RequireLogin
    public Result<String> likeComment(@PathVariable Long id) {
        try {
            commentService.likeComment(id);
            return Result.ok("操作成功");
        } catch (Exception e) {
            log.error("评论点赞失败: {}", e.getMessage(), e);
            return Result.fail("评论点赞失败: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/my")
    @Operation(summary = "我的评论列表")
    @RequireLogin
    public Result<PageResult<CommentsVo>> getMyComments(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            PageResult<CommentsVo> result = commentService.getMyComments(pageNum, pageSize);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("查询我的评论失败: {}", e.getMessage(), e);
            return Result.fail("查询我的评论失败: " + e.getMessage(), 500);
        }
    }
}
