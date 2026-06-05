package com.zcy.forum.controller;

import com.zcy.forum.annotation.RequireLogin;
import com.zcy.forum.common.Result;
import com.zcy.forum.domain.dto.UserMessageDTO;
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
    @Operation(summary ="滚动获取用户会话列表（私聊/群聊/系统通知）" )
    @RequireLogin
    public Result<List<UserConversationVO>> getConversationList(@RequestParam Long userId,@RequestParam(defaultValue = "0") Long lastSeq) {
        return Result.ok(convService.getConversationList(userId,lastSeq));
    }
    @PostMapping("/conversation/clean")
    @Operation(summary = "清空会话未读数")
    public Result<String> clearUnread(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        String conversationId = param.get("conversationId").toString();
        convService.clearUnread(userId, conversationId);
        return Result.ok("操作成功");
    }
    @PostMapping("/conversation/delete")
    @Operation(summary = "删除会话（软删除）")
    public Result<String> deleteConversation(@RequestBody Map<String, Object> param) {
        Long userId = Long.valueOf(param.get("userId").toString());
        String conversationId = param.get("conversationId").toString();
        convService.deleteConversation(userId, conversationId);
        return Result.ok("删除成功");
    }

    @GetMapping("/new")
    @Operation(summary = "增量拉新消息（下拉刷新）")
    public Result<List<UserMessageDTO>> pullNewMessage(
            @RequestParam String conversationId,
            @RequestParam(defaultValue = "0") Long lastMsgTime   // 本地最新消息时间
    ) {
        // 拉新消息不需要 size，有多少拉多少
        return Result.ok(msgService.pullNewMessage(conversationId, lastMsgTime));
    }

    @GetMapping("/history")
    @Operation(summary = "拉取历史消息（上滑加载更多）")
    public Result<List<UserMessageDTO>> pullHistoryMessage(
            @RequestParam String conversationId,
            @RequestParam(defaultValue = "0") Long lastMsgTime,  // 上一页第一条消息的时间
            @RequestParam(defaultValue = "10") Integer size      // 固定10条
    ) {
        return Result.ok(msgService.pullHistoryMessage(conversationId, lastMsgTime, size));
    }


    @PostMapping("/message/read")
    @Operation(summary = "标记单条消息已读")
    public Result<?> readMessage(@RequestBody Map<String, Object> param) {
        String msgId = param.get("msgId").toString();
        msgService.readMessage(msgId);
        return Result.ok(null);
    }

   /* @PostMapping("/message/remove")
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
   */



}
