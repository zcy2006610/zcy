package com.zcy.forum.controller;



import com.zcy.forum.annotation.RequireLogin;
import com.zcy.forum.annotation.SessionMemory;

import com.zcy.forum.common.PageResult;
import com.zcy.forum.common.Result;
import com.zcy.forum.domain.dto.*;
import com.zcy.forum.domain.vo.ChatEventVO;
import com.zcy.forum.domain.vo.ChatMessageVO;
import com.zcy.forum.domain.vo.PostsVo;
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
    @Operation(summary = "阻塞式对话")
    @SessionMemory //会话记忆加会话历史
    @RequireLogin
    public String chat(@RequestBody ChatDTO chatDTO) {
        return aiService.chat(chatDTO);
    }
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式对话")
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
    @PostMapping("/tag")
    @Operation(summary = "AI生成内容标签")
    public Result<List<String>> tags(@RequestBody ChatTextDTO textDTO){
        return Result.ok(aiService.Classify(textDTO));
    }
    @PostMapping("/link")
    @Operation(summary = "AI智能联想")
    @RequireLogin
    public Result<List<String>> link(@RequestBody ChatTextDTO textDTO){
        return Result.ok(aiService.link(textDTO));
    }
    @PostMapping("/search")
    @Operation(summary = "AI智能搜索")
    //TODO
    public Result<PageResult<PostsVo>> SearchWithModel(@RequestBody PageQueryDTO queryDTO ){
        return Result.ok(aiService.AISearch(queryDTO));
    }
    @PostMapping("/recommend")
    @Operation(summary = "AI个性化推荐")
    //TODO
    public Result<List<PostsVo>> recommend(){
        return Result.ok(aiService.recommend());
    }
    @PostMapping("/help")
    @Operation(summary = "AI帮写")
    @RequireLogin
    public Result<String> help(@RequestBody ChatTextDTO textDTO){
        return Result.ok(aiService.generateText(textDTO));
    }
    @PostMapping("/polish")
    @Operation(summary = "AI内容/标题润色")
    @RequireLogin
    public Result<String> polish(@RequestBody ChatTextDTO textDTO){
        return Result.ok(aiService.polishText(textDTO));
    }
    @PostMapping("excerpt")
    @Operation(summary = "AI自动生成摘要")
    //TODO
    public Result<String> excerpt(@RequestBody ChatTextDTO textDTO){
        return null;
    }
    @PostMapping("/translation")
    @Operation(summary = "AI翻译")
    //TODO
    public Result<String> translate(@RequestBody ChatTextDTO textDTO){
      return null;
    }



}
