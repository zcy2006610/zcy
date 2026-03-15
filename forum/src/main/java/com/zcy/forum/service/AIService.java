package com.zcy.forum.service;


import com.zcy.forum.common.PageResult;
import com.zcy.forum.domain.dto.*;
import com.zcy.forum.domain.vo.ChatEventVO;
import com.zcy.forum.domain.vo.ChatMessageVO;
import com.zcy.forum.domain.vo.PostsVo;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AIService {


    String chat(ChatDTO chatDTO);

    Flux<ChatEventVO> chatStream(ChatDTO chatDTO);

    boolean reviewPostV1(PostReviewDTO reviewDTO);

    Long startNewChat(Long userId);

    List<ChatMessageVO> listHistory(Long userId);

    String stopSession(ChatStopDTO stopDTO);

    String generateText(ChatTextDTO textDTO);

    String  polishText(ChatTextDTO textDTO);

    boolean reviewPostV2(PostReviewDTO reviewDTO);

    List<String> Classify(ChatTextDTO textDTO);

    List<String> link(ChatTextDTO textDTO);

    PageResult<PostsVo> AISearch(PageQueryDTO queryDTO);

    List<PostsVo> recommend();
}
