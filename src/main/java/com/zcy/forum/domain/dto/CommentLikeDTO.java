package com.zcy.forum.domain.dto;

import lombok.Data;

@Data
public class CommentLikeDTO {
    private Long commentId;
    private Long userId;
    private Boolean isLike;
    private Integer likeCount;
    private Long timestamp;
}
