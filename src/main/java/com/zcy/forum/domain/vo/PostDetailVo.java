package com.zcy.forum.domain.vo;

import lombok.Data;

@Data
public class PostDetailVo {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Long categoryId;

    private String title;

    private String slug;

    private String content;


    private String cover;

    private String excerpt;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Integer collectCount;

    private Integer shareCount;

    private Integer isTop;

    private Integer isEssence;

    private Integer isHot;

    private Integer isLock;

    private String publisherAvatar;
    private String publisherName;
}
