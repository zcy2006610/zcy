package com.zcy.forum.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResultVO {
    private Long id;
    private Long userId;
    private String result;
    private LocalDateTime reviewTime;
    private String reviewer;
    private String cause;
    private String method;
}
