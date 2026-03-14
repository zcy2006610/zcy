package com.zcy.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户私信表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_messages")
public class UserMessages implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发送者
     */
    private Long fromUserId;

    /**
     * 接收者
     */
    private Long toUserId;

    /**
     * 内容
     */
    private String content;

    /**
     * 0未读 1已读
     */
    private Integer isRead;

    /**
     * 发送者是否删除
     */
    private Integer fromDeleted;

    /**
     * 接收者是否删除
     */
    private Integer toDeleted;

    private LocalDateTime createdAt;


}
