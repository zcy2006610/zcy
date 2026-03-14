package com.zcy.forum.service;


import com.zcy.forum.domain.dto.ChatDTO;
import com.zcy.forum.domain.dto.ChatStopDTO;
import com.zcy.forum.domain.dto.ChatTextDTO;
import com.zcy.forum.domain.dto.PostReviewDTO;
import com.zcy.forum.domain.vo.ChatEventVO;
import com.zcy.forum.domain.vo.ChatMessageVO;
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
}
