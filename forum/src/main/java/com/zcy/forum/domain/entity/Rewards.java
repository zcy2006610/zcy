package com.zcy.forum.domain.entity;

import java.math.BigDecimal;
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
 * 打赏记录表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("rewards")
public class Rewards implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 打赏ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 打赏人ID
     */
    private Long userId;

    /**
     * 接收人ID
     */
    private Long receiveUserId;

    /**
     * 打赏目标：1-帖子，2-评论
     */
    private Integer targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 打赏金额
     */
    private BigDecimal amount;

    /**
     * 1-成功，0-失败
     */
    private Integer status;

    /**
     * 打赏备注
     */
    private String remark;

    private LocalDateTime createdAt;


}
