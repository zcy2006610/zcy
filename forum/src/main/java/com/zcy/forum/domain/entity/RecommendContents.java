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
 * 推荐位内容表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("recommend_contents")
public class RecommendContents implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 推荐位ID
     */
    private Long positionId;

    /**
     * 内容类型：1-帖子，2-板块，3-外部链接
     */
    private Integer targetType;

    /**
     * 内容ID（外部链接则为0）
     */
    private Long targetId;

    /**
     * 展示标题
     */
    private String title;

    /**
     * 展示封面
     */
    private String cover;

    /**
     * 跳转链接（外部链接用）
     */
    private String link;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 生效时间
     */
    private LocalDateTime startTime;

    /**
     * 失效时间（NULL为永久）
     */
    private LocalDateTime endTime;

    /**
     * 0-禁用，1-启用
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
