package com.zcy.forum.controller;

import com.zcy.forum.common.Result;
import com.zcy.forum.domain.entity.ConversationMessage;
import com.zcy.forum.domain.entity.UserConversation;
import com.zcy.forum.domain.vo.ConversationMessageVO;
import com.zcy.forum.domain.vo.UserConversationVO;
import com.zcy.forum.service.IConversationMessageService;
import com.zcy.forum.service.IUserConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notice")
@Tag(name = "消息模块")
public class MessageController {
    @Autowired
    private IConversationMessageService msgService;

    @Autowired
    private IUserConversationService convService;

    @GetMapping("/conversation/list")
    @Operation(summary ="获取用户所有会话列表（私聊/群聊/系统通知）" )
    public Result<List<UserConversationVO>> getConversationList(@RequestParam Long userId) {
        return Result.ok(convService.getConversationList(userId));
    }
    @PostMapping("/conversation/clean")
    @Operation(summary = "清空会话未读数")
    public Result<?> clearUnread(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        String conversationId = param.get("conversationId").toString();
        convService.clearUnread(userId, conversationId);
        return Result.ok();
    }
    @PostMapping("/conversation/delete")
    @Operation(summary = "删除会话（软删除）")
    public Result<?> deleteConversation(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        String conversationId = param.get("conversationId").toString();
        convService.deleteConversation(userId, conversationId);
        return Result.ok();
    }

    @GetMapping("/message/pull")
    @Operation(summary ="增量拉取新消息" )
    public Result<List<ConversationMessage>> pullNewMessage(
            @RequestParam String conversationId,
            @RequestParam Long lastSeq) {
        return Result.ok(msgService.pullNewMessage(conversationId, lastSeq));
    }

    @GetMapping("/message/history")
    @Operation(summary = "拉取历史消息（下拉加载更早消息）")
    public Result<?> pullHistoryMessage(
            @RequestParam String conversationId,
            @RequestParam Long minSeq,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(msgService.pullHistoryMessage(conversationId, minSeq, pageSize));
    }

    @PostMapping("/message/read")
    @Operation(summary = "标记单条消息已读")
    public Result<?> readMessage(@RequestBody Map<String, Object> param) {
        String msgId = param.get("msgId").toString();
        msgService.readMessage(msgId);
        return Result.ok(null);
    }

    @PostMapping("/message/remove")
    @Operation(summary = "删除/撤回消息")
    public Result<?> removeMessage(@RequestBody Map<String, Object> param) {
        String msgId = param.get("msgId").toString();
        Long senderId = Long.valueOf(param.get("senderId").toString());
        msgService.removeMessage(msgId, senderId);
        return Result.ok(null);
    }

    @GetMapping("/systemmsg/get")
    @Operation(summary = "获取用户系统通知会话")
    public Result<?> getSystemNoticeConversation(@RequestParam Long userId) {
        return Result.ok(convService.getSystemConversation(userId));
    }

    @GetMapping("/systemmsg/list")
    @Operation(summary = "拉取系统通知消息")
    public Result<?> getSystemNoticeList(
            @RequestParam Long userId,
            @RequestParam Long lastSeq) {
        return Result.ok(msgService.pullSystemNotice(userId, lastSeq));
    }

    @PostMapping("/systemmsg/send")
    @Operation()
    public Result<?> sendMessage(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        Integer convType = Integer.valueOf(param.get("convType").toString());
        String content = param.get("content").toString();
        Object extend = param.get("extend");

        msgService.sendCommonMessage(userId, convType, content, extend);
        return Result.ok(null);
    }
    @PostMapping("/online")
    @Operation(summary = "用户上线")
    public Result<?> userOnline(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        String clientId = param.get("clientId").toString();
        convService.userOnline(userId, clientId);
        return Result.ok(null);
    }

    @PostMapping("/offline")
    @Operation(summary = "用户离线")
    public Result<?> userOffline(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        convService.userOffline(userId);
        return Result.ok(null);
    }



}
