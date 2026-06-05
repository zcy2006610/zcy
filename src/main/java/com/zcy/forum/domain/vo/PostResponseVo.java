package com.zcy.forum.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponseVo {

   private Long maxTime;
   private Long lastId;
   private List<PostsVo> vos;
}
