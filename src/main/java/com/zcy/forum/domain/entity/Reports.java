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
 * 举报/反馈表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("reports")
public class Reports implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 举报ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 举报人ID
     */
    private Long userId;

    /**
     * 举报目标：1-帖子，2-评论，3-用户
     */
    private Integer targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 举报类型：1-广告，2-色情，3-辱骂，4-抄袭，5-其他
     */
    private Integer reasonType;

    /**
     * 举报理由
     */
    private String reason;

    /**
     * 处理状态：0-待处理，1-已受理，2-已驳回，3-已处理
     */
    private Integer status;

    /**
     * 处理人ID
     */
    private Long handleUserId;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
