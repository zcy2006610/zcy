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
 * 积分规则表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("point_rules")
public class PointRules implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 行为标识（如post_publish、comment）
     */
    private String action;

    /**
     * 行为名称（如发布帖子、评论）
     */
    private String actionName;

    /**
     * 积分值（可正可负）
     */
    private Integer point;

    /**
     * 每日上限（0为无限制）
     */
    private Integer dailyLimit;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
