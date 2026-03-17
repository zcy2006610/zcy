package com.zcy.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 用户会话表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-17
 */
@TableName("user_conversation")
public class UserConversation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
    public Integer getConvType() {
        return convType;
    }

    public void setConvType(Integer convType) {
        this.convType = convType;
    }
    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
    public Long getLastSeq() {
        return lastSeq;
    }

    public void setLastSeq(Long lastSeq) {
        this.lastSeq = lastSeq;
    }
    public String getLastContent() {
        return lastContent;
    }

    public void setLastContent(String lastContent) {
        this.lastContent = lastContent;
    }
    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "UserConversation{" +
            "id=" + id +
            ", conversationId=" + conversationId +
            ", userId=" + userId +
            ", targetId=" + targetId +
            ", convType=" + convType +
            ", unreadCount=" + unreadCount +
            ", lastSeq=" + lastSeq +
            ", lastContent=" + lastContent +
            ", isDeleted=" + isDeleted +
            ", createTime=" + createTime +
            ", updateTime=" + updateTime +
        "}";
    }
}
