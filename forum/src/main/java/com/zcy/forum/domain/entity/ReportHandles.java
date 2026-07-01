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
 * 举报处理结果表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("report_handles")
public class ReportHandles implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 处理记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 举报目标：1-帖子，2-评论，3-用户
     */
    private Integer targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 处理结果：1-警告，2-删除内容，3-封禁账号，4-驳回
     */
    private Integer handleResult;

    /**
     * 处理内容（如“删除评论ID:456，通知用户整改”）
     */
    private String handleContent;

    /**
     * 处理管理员ID
     */
    private Long operatorId;

    /**
     * 关联举报ID（多个用,分隔）
     */
    private String relatedReportIds;

    private LocalDateTime createdAt;


}
