package com.zcy.forum.domain.vo;

import lombok.Data;

@Data
public class ConversationMessageVO {
    private Long id;

    /**
     * 消息唯一ID（UUID）
     */
    private String msgId;

    /**
     * 关联会话ID
     */
    private String conversationId;

    /**
     * 会话类型：1=私聊 2=群聊 3=系统通知
     */
    private Integer convType;

    /**
     * 消息类型：1=文本 2=系统通知 3=公告 4=业务通知
     */
    private Integer msgType;

    /**
     * 发送者ID：系统通知=0
     */
    private Long senderId;

    /**
     * 接收者ID（群聊可为空）
     */
    private Long receiverId;

    /**
     * 【核心游标】会话内自增序列号，用于增量拉取消息
     */
    private Long seq;

    /**
     * 消息内容/系统通知内容
     */
    private String content;

    /**
     * 扩展字段（业务参数：订单号/跳转链接等）
     */
    private String extend;

    /**
     * 消息状态：0=正常 1=撤回 2=删除
     */
    private Integer status;

}
