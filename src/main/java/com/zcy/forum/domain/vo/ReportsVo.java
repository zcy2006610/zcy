package com.zcy.forum.domain.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ReportsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Integer targetType;

    private Long targetId;

    private Integer reasonType;

    private String reason;

    private Integer status;

    private Long handleUserId;

    private String handleRemark;

    private LocalDateTime handleTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
