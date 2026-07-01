package com.zcy.forum.domain.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserBansVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Integer banType;

    private String reason;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long operatorId;

    private Integer isLifted;

    private LocalDateTime liftTime;

    private Long liftOperatorId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
