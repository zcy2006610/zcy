package com.zcy.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.io.Serializable;


/**
 * <p>
 * 系统公告
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("announcements")
public class Announcements implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    /**
     * 优先级 越大越靠前
     */
    private Integer priority;

    /**
     * 0下架 1上架
     */
    private Integer status;

    /**
     * 管理员ID
     */
    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
