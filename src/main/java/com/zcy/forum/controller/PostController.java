package com.zcy.forum.controller;

import com.zcy.forum.annotation.RequireLogin;
import com.zcy.forum.common.PageResult;
import com.zcy.forum.common.Result;
import com.zcy.forum.domain.dto.Post2DraftDTO;
import com.zcy.forum.domain.dto.PostEditDTO;
import com.zcy.forum.domain.dto.PostPublishDTO;
import com.zcy.forum.domain.dto.PostUpdateDTO;
import com.zcy.forum.domain.vo.PostDetailVo;
import com.zcy.forum.domain.vo.PostDraftsVo;
import com.zcy.forum.domain.vo.PostResponseVo;
import com.zcy.forum.domain.vo.PostsVo;
import com.zcy.forum.service.PostService;
import com.zcy.forum.utils.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/post")
@Tag(name="帖子模块")
@Slf4j
public class PostController {

    @Autowired
    private PostService postService;
    @Operation(summary = "分页查询模块下的帖子(可带条件)")
    @GetMapping("/page")
    public Result<PageResult<PostsVo>> ScrollPage(@RequestParam Integer pageNum,
                                                  @RequestParam Integer pageSize,
                                                  @RequestParam Map<String,Object> params
                                                  ){

        return Result.ok(postService.getPage(pageNum,pageSize,params));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "查询帖子详情")
    public Result<PostDetailVo> detail(@PathVariable Long id){
        return Result.ok(postService.detail(id));
    }

    @PutMapping("/publish")
    @Operation(summary = "发布帖子")
    @RequireLogin
    public Result<Long> publishPost(@RequestBody @Validated PostPublishDTO publishDTO){
        try {
            // 验证分类ID是否存在（后续可在service层实现）
            Long postId = postService.publish(publishDTO);
            return Result.ok(postId);
        } catch (Exception e) {
            // 记录错误日志
            log.error("发布帖子失败: {}", e.getMessage(), e);
            // 返回错误信息
            return Result.fail("发布帖子失败: " ,500 );
        }
    }

    @PostMapping("/update/{id}")
    @Operation(summary = "修改帖子")
    @RequireLogin
    public Result<String> updatePost(@PathVariable("id") Long id, @RequestBody @Validated PostUpdateDTO updateDTO){
        try {
            updateDTO.setId(id);
            postService.updatePost(updateDTO);
            return Result.ok("修改帖子成功");
        } catch (Exception e) {
            log.error("修改帖子失败: {}", e.getMessage(), e);
            return Result.fail("修改帖子失败: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/trace/{id}")
    @Operation(summary = "根据id回显将要修改的帖子信息")
    @RequireLogin
    public Result<PostUpdateDTO> trace(@PathVariable("id") Long id){
        try {
            PostUpdateDTO dto = postService.trace(id);
            return Result.ok(dto);
        } catch (Exception e) {
            log.error("获取帖子信息失败: {}", e.getMessage(), e);
            return Result.fail("获取帖子信息失败: " + e.getMessage(), 500);
        }
    }

    @DeleteMapping("/logicdelete/{id}")
    @Operation(summary = "逻辑删除帖子")
    @RequireLogin
    public Result<String> logicDelete(@PathVariable("id") Long id){
        try {
            postService.logicDelete(id);
            return Result.ok("删除帖子成功");
        } catch (Exception e) {
            log.error("删除帖子失败: {}", e.getMessage(), e);
            return Result.fail("删除帖子失败: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/mypost")
    @Operation(summary = "查询我发布过的帖子")
    @RequireLogin
    public Result<List<PostsVo>> queryMyPost(){
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        try {
            List<PostsVo> posts = postService.queryMyPost(userId);
            return Result.ok(posts);
        } catch (Exception e) {
            log.error("查询我的帖子失败: {}", e.getMessage(), e);
            return Result.fail("查询我的帖子失败: " + e.getMessage(), 500);
        }
    }

    @PostMapping("/edit")
    @Operation(summary = "编辑帖子")
    @RequireLogin
    public Result<String> editMyPost(@RequestBody PostEditDTO editDTO){
        try {
            postService.editMyPost(editDTO);
            return Result.ok("编辑帖子成功");
        } catch (Exception e) {
            log.error("编辑帖子失败: {}", e.getMessage(), e);
            return Result.fail("编辑帖子失败: " + e.getMessage(), 500);
        }
    }

    @PostMapping("/draft")
    @Operation(summary = "未发布的帖子存入草稿箱")
    @RequireLogin
    public Result<Long> save2draft(@RequestBody Post2DraftDTO draftDTO){
        try {
            Long draftId = postService.save2draft(draftDTO);
            return Result.ok(draftId);
        } catch (Exception e) {
            log.error("保存草稿失败: {}", e.getMessage(), e);
            return Result.fail("保存草稿失败: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/getdraft/{id}")
    @Operation(summary = "查询我的草稿箱")
    @RequireLogin
    public Result<PostDraftsVo> getMyDraft(@PathVariable Long id){
        return Result.ok(postService.getDraft(id));
    }



   





}
