package com.zcy.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 聊天会话表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_conversation")
public class ChatConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID（雪花ID）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话类型：1-单聊 2-群聊 3-用户与AI对话
     */
    private Integer conversationType;

    /**
     * 会话标题（AI对话自动生成）
     */
    private String title;

    /**
     * 会话创建人ID
     */
    private Long createUserId;

    /**
     * 群ID（单聊/AI对话为0）
     */
    private Long groupId;

    /**
     * 单聊对方用户ID（群聊/AI对话为0）
     */
    private Long targetUserId;

    /**
     * AI对话模型（如gpt-3.5、讯飞星火）
     */
    private String aiModel;

    /**
     * 最后一条消息ID
     */
    private Long lastMsgId;

    /**
     * 最后一条消息内容（预览）
     */
    private String lastMsgContent;

    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMsgTime;

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
