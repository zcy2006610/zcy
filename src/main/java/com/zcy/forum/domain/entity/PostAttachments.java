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
 * 帖子附件表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("post_attachments")
public class PostAttachments implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 附件ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 上传者ID
     */
    private Long userId;

    /**
     * 附件原名称
     */
    private String name;

    /**
     * 附件存储路径
     */
    private String path;

    /**
     * 附件访问URL
     */
    private String url;

    /**
     * 附件大小（字节）
     */
    private Long size;

    /**
     * 附件类型（如image/jpeg、application/pdf）
     */
    private String type;

    /**
     * 文件扩展名
     */
    private String fileExt;

    /**
     * 排序
     */
    private Integer sort;

    private LocalDateTime createdAt;


}
