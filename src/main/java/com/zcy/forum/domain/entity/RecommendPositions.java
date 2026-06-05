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
 * 推荐位配置表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("recommend_positions")
public class RecommendPositions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 推荐位ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 推荐位名称：首页轮播/板块置顶推荐
     */
    private String name;

    /**
     * 推荐位标识：index_carousel/category_recommend
     */
    private String code;

    /**
     * 排序权重
     */
    private Integer sort;

    /**
     * 0-禁用，1-启用
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
