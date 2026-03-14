package com.zcy.forum.domain.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class VoteRecordsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long voteId;

    private Long userId;

    private String optionIds;

    private LocalDateTime createdAt;
}
