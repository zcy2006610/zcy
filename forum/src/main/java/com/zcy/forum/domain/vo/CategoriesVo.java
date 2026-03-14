package com.zcy.forum.domain.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.zcy.forum.domain.entity.Categories;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data

public class CategoriesVo {

    private Long id;

    private String name;

    private String slug;

    private String description;

    private String icon;

    private Long parentId;

    private Integer sort;


    private Integer status;

    private Long adminId;


}
