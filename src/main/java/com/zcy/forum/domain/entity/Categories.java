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
 * 板块/分类表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("categories")
public class Categories implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 板块ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 板块名称
     */
    private String name;

    /**
     * 板块别名（URL友好，如tech-forum）
     */
    private String slug;

    /**
     * 板块描述
     */
    private String description;

    /**
     * 板块图标URL
     */
    private String icon;

    /**
     * 父板块ID（0为顶级）
     */
    private Long parentId;

    /**
     * 排序权重（越大越靠前）
     */
    private Integer sort;

    /**
     * 板块帖子数（缓存）
     */
    private Integer postCount;

    /**
     * 今日新增帖子数
     */
    private Integer todayPostCount;

    /**
     * 状态：0-隐藏，1-显示，2-维护中
     */
    private Integer status;

    /**
     * 板块版主ID
     */
    private Long adminId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;


}
