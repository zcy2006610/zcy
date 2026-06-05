package com.zcy.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 会话消息表（含系统通知）
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-17
 */
@TableName("conversation_message")
public class ConversationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    public Integer getConvType() {
        return convType;
    }

    public void setConvType(Integer convType) {
        this.convType = convType;
    }
    public Integer getMsgType() {
        return msgType;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }
    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }
    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ConversationMessage{" +
            "id=" + id +
            ", msgId=" + msgId +
            ", conversationId=" + conversationId +
            ", convType=" + convType +
            ", msgType=" + msgType +
            ", senderId=" + senderId +
            ", receiverId=" + receiverId +
            ", seq=" + seq +
            ", content=" + content +
            ", extend=" + extend +
            ", status=" + status +
            ", createTime=" + createTime +
        "}";
    }
}
