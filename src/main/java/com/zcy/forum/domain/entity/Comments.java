package com.zcy.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 评论/回复表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("comments")
public class Comments implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评论者ID
     */
    private Long userId;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 父评论ID（0为一级评论）
     */
    private Long parentId;

    /**
     * 被回复者ID
     */
    private Long replyUserId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 渲染后的HTML内容
     */


    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 是否楼主回复：0-否，1-是
     */
    private Integer isAuthorReply;

    /**
     * 是否显示：0-隐藏，1-显示
     */
    private Integer isShow;

    /**
     * 审核状态：0-待审核，1-通过，2-拒绝
     */
    private Integer auditStatus;

    /**
     * 状态：0-软删除，1-正常，2-违规屏蔽
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;


}
