package com.zcy.forum.domain.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CommentsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Long postId;

    private Long parentId;

    private Long replyUserId;

    private String content;

    private Integer likeCount;

    private Integer isAuthorReply;

    private LocalDateTime createdAt;

    // 评论者信息
    private String commenterName;
    private String commenterAvatar;

    // 被回复者信息
    private String replyUserName;

}
