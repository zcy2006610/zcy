package com.zcy.forum.domain.dto;

import lombok.Data;

@Data
public class PostEditDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
}
