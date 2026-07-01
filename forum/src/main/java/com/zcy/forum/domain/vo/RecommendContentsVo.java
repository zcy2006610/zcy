package com.zcy.forum.domain.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RecommendContentsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long positionId;

    private Integer targetType;

    private Long targetId;

    private String title;

    private String cover;

    private String link;

    private Integer sort;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
