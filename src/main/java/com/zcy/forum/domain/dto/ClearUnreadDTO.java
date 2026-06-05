package com.zcy.forum.domain.dto;

import lombok.Data;

@Data
public class ClearUnreadDTO {
    private Long userId;
    private String conversationId;
}