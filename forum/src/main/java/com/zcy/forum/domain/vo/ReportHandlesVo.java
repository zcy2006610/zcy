package com.zcy.forum.domain.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ReportHandlesVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer targetType;

    private Long targetId;

    private Integer handleResult;

    private String handleContent;

    private Long operatorId;

    private String relatedReportIds;

    private LocalDateTime createdAt;
}
