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
 * 用户封禁记录表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_bans")
public class UserBans implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 封禁ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 被封禁用户ID
     */
    private Long userId;

    /**
     * 封禁类型：1-禁言，2-禁止发帖，3-账号封禁
     */
    private Integer banType;

    /**
     * 封禁原因
     */
    private String reason;

    /**
     * 封禁开始时间
     */
    private LocalDateTime startTime;

    /**
     * 解封时间（NULL为永久）
     */
    private LocalDateTime endTime;

    /**
     * 操作管理员ID
     */
    private Long operatorId;

    /**
     * 是否解封：0-未解封，1-已解封
     */
    private Integer isLifted;

    /**
     * 解封时间
     */
    private LocalDateTime liftTime;

    /**
     * 解封管理员ID
     */
    private Long liftOperatorId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
