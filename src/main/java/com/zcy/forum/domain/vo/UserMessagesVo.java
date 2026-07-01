package com.zcy.forum.domain.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserMessagesVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private String content;

    private Integer isRead;

    private Integer fromDeleted;

    private Integer toDeleted;

    private LocalDateTime createdAt;
}
