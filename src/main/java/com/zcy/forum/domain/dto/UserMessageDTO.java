package com.zcy.forum.domain.dto;

import lombok.Data;

@Data
public class UserMessageDTO {
    private Long userId;
    private Long conversationId; //会话id
    private Long toUserId;    // 接收人ID
    private String content;   // 消息内容 / 图片URL
    private Integer type;    // 0=文本 1=图片
}