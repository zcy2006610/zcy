package com.zcy.forum.domain.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PointRulesVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String action;

    private String actionName;

    private Integer point;

    private Integer dailyLimit;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
