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
 * 积分变动日志表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("point_logs")
public class PointLogs implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 行为标识
     */
    private String action;

    /**
     * 变动积分（+10/-5）
     */
    private Integer point;

    /**
     * 变动后积分余额
     */
    private Integer balance;

    /**
     * 关联ID（如帖子ID）
     */
    private Long relatedId;

    /**
     * 备注
     */
    private String remark;

    private LocalDateTime createdAt;


}
