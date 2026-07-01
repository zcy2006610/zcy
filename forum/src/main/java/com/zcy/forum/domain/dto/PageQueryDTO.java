package com.zcy.forum.domain.dto;

import lombok.Data;

@Data
public class PageQueryDTO {
    private Integer pageNum;
    private Integer pageSize;
    private String key;

}
