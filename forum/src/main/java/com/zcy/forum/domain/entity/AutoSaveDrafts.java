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
 * 自动备份草稿表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auto_save_drafts")
public class AutoSaveDrafts implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 备份ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 类型：1-帖子，2-评论
     */
    private Integer type;

    /**
     * 关联ID（如帖子ID/评论ID）
     */
    private Long relatedId;

    /**
     * 备份内容
     */
    private String content;

    /**
     * 备份时间
     */
    private LocalDateTime saveTime;

    /**
     * 是否过期（7天自动过期）
     */
    private Integer isExpired;


}
