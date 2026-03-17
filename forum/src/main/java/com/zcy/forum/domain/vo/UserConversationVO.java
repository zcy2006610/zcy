package com.zcy.forum.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserConversationVO {
    private Long id;

    /**
     * 会话唯一ID（UUID）
     */
    private String conversationId;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 目标ID：好友ID/群ID/系统通知=0
     */
    private Long targetId;

    /**
     * 会话类型：1=私聊 2=群聊 3=系统通知
     */
    private Integer convType;

    /**
     * 未读消息数量
     */
    private Integer unreadCount;

    /**
     * 会话最后一条消息的seq（游标）
     */
    private Long lastSeq;

    /**
     * 最后一条消息内容
     */
    private String lastContent;

    /**
     * 是否删除：0=否 1=是
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


    //聊天目标用户头像地址
    private String targetUrl;
}
