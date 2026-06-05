package com.zcy.forum.domain.vo;

import lombok.Data;

@Data
public class HotPostVO {
    private Long id;
    private Long categoryId;
    private String title;
    private String excerpt;
}
