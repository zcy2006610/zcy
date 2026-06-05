package com.zcy.forum.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentPublishDTO {
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    @NotNull(message = "评论内容不能为空")
    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long parentId; // 父评论ID，0为一级评论
}
