package com.zcy.forum.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatDTO {
    private Long userId;
    private Long currentSessionId;
    private String message;

}
