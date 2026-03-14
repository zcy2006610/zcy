package com.zcy.forum.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostUpdateDTO {
    @NotNull
    private Long id;

    private Long userId;

    @NotNull
    private Long categoryId;
    private String title;
    private String content;
    private String cover;
}
