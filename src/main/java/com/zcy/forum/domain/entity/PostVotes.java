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
 * 帖子投票表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("post_votes")
public class PostVotes implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 投票ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联帖子ID
     */
    private Long postId;

    /**
     * 投票标题
     */
    private String title;

    /**
     * 投票选项：[{"id":1,"name":"选项1","count":0}]
     */
    private String options;

    /**
     * 是否多选：0-否，1-是
     */
    private Integer isMulti;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 1-进行中，2-已结束，0-已关闭
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
