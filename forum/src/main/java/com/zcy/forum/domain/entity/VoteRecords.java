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
 * 投票记录表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("vote_records")
public class VoteRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 投票记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 投票ID
     */
    private Long voteId;

    /**
     * 投票用户ID
     */
    private Long userId;

    /**
     * 选中的选项ID（多个用,分隔）
     */
    private String optionIds;

    private LocalDateTime createdAt;


}
