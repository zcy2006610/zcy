package com.zcy.forum.domain.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserLoginLogsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Integer loginType;

    private String ip;

    private String device;

    private String location;

    private Integer status;

    private String errorMsg;

    private LocalDateTime createdAt;
}
