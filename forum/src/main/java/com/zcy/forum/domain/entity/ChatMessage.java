package com.zcy.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * <p>
 * 聊天消息表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_message")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID（雪花ID）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联会话ID
     */
    private Long conversationId;

    /**
     * 发送者ID（AI对话：用户ID/AI标识）
     */
    private Long senderId;

    /**
     * 接收者ID（单聊=对方ID，群聊=群ID，AI对话=0）
     */
    private Long receiverId;

    /**
     * 消息类型：1-文本 2-图片 3-语音 4-文件 5-AI回复
     */
    private Integer messageType;

    /**
     * 消息内容（文本直接存，多媒体存URL/文件ID）
     */
    private String content;

    /**
     * 扩展字段（AI tokens数、图片宽高、语音时长等）
     */
    private String contentExt;

    /**
     * 消息状态：0-发送中 1-已发送 2-已读 3-失败 4-撤回
     */
    private Integer messageStatus;

    /**
     * 关联Redis会话记忆的key
     */
    private String aiMemoryKey;

    /**
     * 软删除：0-未删 1-已删
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


}
