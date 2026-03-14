package com.zcy.ai.domain.dto;

import lombok.Data;

@Data
public class PostReviewDTO {
    private Long id;
    private Long userId;
    private String title;
    private String slug;
    private String content;
    private String excerpt;
}
