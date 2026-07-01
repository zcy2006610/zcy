package com.zcy.forum.domain.dto;

import lombok.Data;

@Data
public class DeleteConversationDTO {
    private Long userId;
    private String conversationId;
}
