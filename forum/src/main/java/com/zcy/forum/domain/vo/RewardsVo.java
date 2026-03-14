package com.zcy.forum.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RewardsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Long receiveUserId;

    private Integer targetType;

    private Long targetId;

    private BigDecimal amount;

    private Integer status;

    private String remark;

    private LocalDateTime createdAt;
}
