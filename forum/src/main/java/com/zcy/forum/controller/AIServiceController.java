package com.zcy.forum.controller;



import com.zcy.forum.annotation.RequireLogin;
import com.zcy.forum.annotation.SessionMemory;

import com.zcy.forum.common.Result;
import com.zcy.forum.domain.dto.ChatDTO;
import com.zcy.forum.domain.dto.ChatStopDTO;
import com.zcy.forum.domain.dto.ChatTextDTO;
import com.zcy.forum.domain.dto.PostReviewDTO;
import com.zcy.forum.domain.vo.ChatEventVO;
import com.zcy.forum.domain.vo.ChatMessageVO;
import com.zcy.forum.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/service")
@Tag(name = "AI服务相关接口")
public class AIServiceController {

    private final AIService aiService;

    @PostMapping("/chat")
    @Operation(summary = "阻塞式聊天")
    @SessionMemory //会话记忆加会话历史
    @RequireLogin
    public String chat(@RequestBody ChatDTO chatDTO) {
        return aiService.chat(chatDTO);
    }


    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式聊天")
    @RequireLogin
    public Flux<ChatEventVO> chatStream(@RequestBody ChatDTO chatDTO) {
        return aiService.chatStream(chatDTO);
    }

    @GetMapping("/newSession/{userId}")
    @Operation(summary = "新建会话")
    @RequireLogin
    public Long newSession(@PathVariable Long userId){
        return aiService.startNewChat(userId);
    }

    @Operation(summary = "查询我的会话历史")
    @GetMapping("/history/{userId}")
    @RequireLogin
    public List<ChatMessageVO> listMyHistory(@PathVariable Long userId){
        return aiService.listHistory(userId);
    }

    @PostMapping("/stop")
    @Operation(summary = "停止对话")
    @RequireLogin
    public String stopSession(@RequestBody ChatStopDTO stopDTO){
        return aiService.stopSession(stopDTO);
    }

    @PostMapping("/help")
    @Operation(summary = "AI帮写")
    //TODO
    public Result<String> help(@RequestBody ChatTextDTO textDTO){
        return Result.ok(aiService.generateText(textDTO));
    }

    @PostMapping("/polish")
    @Operation(summary = "AI润色")
    //TODO
    public Result<String> polish(@RequestBody ChatTextDTO textDTO){
        return Result.ok(aiService.polishText(textDTO));
    }

    @PostMapping("/tag")
    @Operation(summary = "AI内容打标签")
    public Result<List<String>> tags(@RequestBody ChatTextDTO textDTO){
        return Result.ok(aiService.Classify(textDTO));
    }




}
