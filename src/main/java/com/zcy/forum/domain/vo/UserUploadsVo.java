package com.zcy.forum.domain.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserUploadsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String fileName;

    private String fileUrl;

    private Integer fileSize;

    private String fileType;

    private Integer used;

    private LocalDateTime createdAt;
}
