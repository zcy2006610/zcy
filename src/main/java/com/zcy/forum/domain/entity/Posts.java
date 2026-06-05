package com.zcy.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.io.Serializable;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 帖子核心表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("posts")
public class Posts implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 帖子ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 所属板块ID
     */
    private Long categoryId;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子别名（URL友好）
     */
    private String slug;

    /**
     * 帖子内容（Markdown/HTML）
     */
    private String content;

    /**
     * 渲染后的HTML内容（缓存）
     */

    /**
     * 封面图URL
     */
    private String cover;

    /**
     * 帖子摘要
     */
    private String excerpt;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 分享数
     */
    private Integer shareCount;

    /**
     * 是否置顶：0-否，1-板块置顶，2-全站置顶
     */
    private Integer isTop;

    /**
     * 是否精华：0-否，1-板块精华，2-全站精华
     */
    private Integer isEssence;

    /**
     * 是否热门：0-否，1-是
     */
    private Integer isHot;

    /**
     * 是否锁定（禁止评论）：0-否，1-是
     */
    private Integer isLock;

    /**
     * 审核状态：0-待审核，1-审核通过，2-审核拒绝
     */
    private Integer auditStatus;

    /**
     * 审核人ID
     */
    private Long auditUserId;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 状态：0-软删除，1-正常，2-违规屏蔽，3-回收站
     */
    private Integer status;


    private Date createdAt;

    private Date updatedAt;

    private Date deletedAt;


}
