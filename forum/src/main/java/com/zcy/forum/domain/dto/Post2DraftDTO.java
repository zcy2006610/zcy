package com.zcy.forum.domain.dto;

import lombok.Data;

@Data
public class Post2DraftDTO {
    private Long userId;
    private Long categoryId;
    private String title;
    private String content;
}
