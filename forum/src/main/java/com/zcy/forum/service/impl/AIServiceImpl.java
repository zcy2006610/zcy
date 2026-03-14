package com.zcy.forum.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.zcy.forum.constant.ChatEventTypeEnum;
import com.zcy.forum.constant.Constant;
import com.zcy.forum.constant.ResultConstant;
import com.zcy.forum.domain.dto.ChatDTO;
import com.zcy.forum.domain.dto.ChatStopDTO;
import com.zcy.forum.domain.dto.ChatTextDTO;
import com.zcy.forum.domain.dto.PostReviewDTO;
import com.zcy.forum.domain.entity.ChatConversation;
import com.zcy.forum.domain.entity.ChatMessage;
import com.zcy.forum.domain.vo.ChatEventVO;
import com.zcy.forum.domain.vo.ChatMessageVO;
import com.zcy.forum.mapper.AIServiceMapper;
import com.zcy.forum.mapper.ChatConversationMapper;
import com.zcy.forum.mapper.ChatMessageMapper;
import com.zcy.forum.service.AIService;
import com.zcy.forum.utils.TextNormalizeUtils;
import com.zcy.forum.utils.ToolResultHolder;
import com.zcy.forum.utils.UserContextHolder;
import groovy.util.logging.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@lombok.extern.slf4j.Slf4j
@Slf4j
@Service
public class AIServiceImpl implements AIService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private AIServiceMapper aiServiceMapper;

    @Autowired
    private ChatConversationMapper conversationMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ThreadPoolTaskExecutor asyncTaskExecutor;

    @Autowired
    private ChatConversationMapper chatConversationMapper;

    // 定义5位数的最小值和最大值
    private static final int MIN_5_DIGIT = 10000;
    private static final int MAX_5_DIGIT = 99999;
    // 单例Random（避免频繁创建，提升性能）
    private static final Random RANDOM = new Random();

    private static final Map<String, Boolean> GENERATE_STATUS = new ConcurrentHashMap<>();

    private static final ChatEventVO STOP_EVENT = ChatEventVO.builder().eventType(ChatEventTypeEnum.STOP.getValue()).build();
    @Override
    public String chat(ChatDTO chatDTO) {
        // 调用聊天客户端处理用户问题并获取响应内容
       String content = this.chatClient.prompt()
                        .system(p->p.param("now", LocalDateTime.now()))
                        .user(chatDTO.getMessage())
                        .call()
                        .content();
        log.info("question: {}, content: {}", chatDTO.getMessage(), content);
        return content;
    }

    @Override
    public Flux<ChatEventVO> chatStream(ChatDTO chatDTO) {

        Long userId = chatDTO.getUserId();
        Long currentSessionId = chatDTO.getCurrentSessionId();
        // 优化：提前拼接key，避免多次拼接
        String key = userId + ":" + currentSessionId;
        String message = chatDTO.getMessage();
        String redisKey = "session:memory:" + userId + ":" + currentSessionId;

        // 1. 修复点：使用线程安全的StringBuffer（或AtomicReference）存储AI回复
        StringBuffer aiReplyBuffer = new StringBuffer("AI: ");
        // 2. 从Redis获取历史记录
        List<String> history = redisTemplate.opsForList().range(redisKey, 0, -1);
        if (history == null) {
            history = new ArrayList<>();
        }
        // 3. 添加用户消息到内存历史（仅内存使用，Redis后续统一处理）
        history.add("用户: " + message);
        // 4. 修复subList问题：创建新列表，避免视图导致的修改风险
        if (history.size() > 20) {
            history = new ArrayList<>(history.subList(history.size() - 20, history.size()));
        }
        List<String> finalHistory = new ArrayList<>(history);

        String requestId= IdUtil.fastSimpleUUID();
        return this.chatClient.prompt()
                .system(p->{
                    p.text(Constant.SYSTEM_ROLE);
                })
                .toolContext(MapUtil.<String, Object>builder() // 设置tool列表
                        .put(requestId, requestId) // 设置请求id参数
                        .build())
                .user(String.join("\n", finalHistory))
                .stream()
                .chatResponse()
                .doFirst(() -> {  // 输出开始，标记正在输出
                    GENERATE_STATUS.put(key, true);
                })
                .doOnComplete(() -> {
                    GENERATE_STATUS.remove(key);
                    // 5. 修复：AI回复加入历史记录（线程安全）
                    String finalAiReply = aiReplyBuffer.toString();
                    finalHistory.add(finalAiReply);

                    // 异步写入Redis（修复重复插入、截断逻辑）
                    CompletableFuture.runAsync(() -> {
                        redisTemplate.executePipelined((RedisCallback<?>) connection -> {
                            byte[] redisKeyBytes = redisKey.getBytes();
                            // 先获取Redis中的当前长度
                            Long currentSize = redisTemplate.opsForList().size(redisKey);
                            currentSize = currentSize == null ? 0 : currentSize;

                            // 修复：截断逻辑（保证Redis中总长度不超过20）
                            int maxHistorySize = 20;
                            if (currentSize >= maxHistorySize) {
                                // 保留最后19条，为新消息留出位置
                                connection.lTrim(redisKeyBytes, currentSize - (maxHistorySize - 1), -1);
                            }

                            // 写入用户消息（仅一次，避免重复）
                            byte[] userMsgBytes = ("用户: " + message).getBytes();
                            connection.rPush(redisKeyBytes, userMsgBytes);

                            // 写入AI回复（非空判断）
                            if (!finalAiReply.trim().isEmpty()) {
                                byte[] aiMsgBytes = finalAiReply.getBytes();
                                connection.rPush(redisKeyBytes, aiMsgBytes);
                            }

                            // 设置过期时间（24小时）
                            connection.expire(redisKeyBytes, 60 * 60 * 24);
                            return null;
                        });
                    }, asyncTaskExecutor);

                    // 异步保存聊天记录（补全保存逻辑）
                    CompletableFuture.runAsync(() -> {
                        String aiReply = aiReplyBuffer.toString().replace("AI: ", "");
                        String content = "用户: " + message + " AI回复: " + aiReply;
                        ChatMessage chatMessage = ChatMessage.builder()
                                .conversationId(currentSessionId)
                                .senderId(userId)
                                .receiverId(0L)
                                .content(content)
                                .messageType(1) // 1-文本消息
                                .messageStatus(1) // 1-已发送
                                .aiMemoryKey(redisKey)
                                .isDeleted(0)
                                .createTime(LocalDateTime.now())
                                .updateTime(LocalDateTime.now())
                                .build();
                        // 补全：保存ChatMessage到数据库（示例）
                         chatMessageMapper.insert(chatMessage);
                    }, asyncTaskExecutor);
                })
                .doOnError(throwable -> {
                    GENERATE_STATUS.remove(key);
                    // 异常时清理状态，避免内存泄漏
                })
                .takeWhile(s -> GENERATE_STATUS.getOrDefault(key, false))
                .map(chatResponse -> {
                    // 获取大模型流式输出的内容
                    String text = chatResponse.getResult().getOutput().getText();
                    // 线程安全的追加（StringBuffer是线程安全的）
                    aiReplyBuffer.append(text);
                    // 封装响应对象
                    return ChatEventVO.builder()
                            .eventData(text)
                            .eventType(ChatEventTypeEnum.DATA.getValue())
                            .build();
                })
                .concatWith(Flux.defer(() -> {
                    // 通过请求id获取到参数列表，如果不为空，就将其追加到返回结果中
                    var map = ToolResultHolder.get(requestId);
                    if (CollUtil.isNotEmpty(map)) {
                        ToolResultHolder.remove(requestId); // 清除参数列表

                        // 响应给前端的参数数据
                        ChatEventVO chatEventVO = ChatEventVO.builder()
                                .eventData((String)map.get("posts") )
                                .eventType(ChatEventTypeEnum.PARAM.getValue())
                                .build();
                        return Flux.just(chatEventVO, STOP_EVENT);
                    }
                    return Flux.just(STOP_EVENT);
                }));
    }

    @Override
    public boolean reviewPostV1(PostReviewDTO reviewDTO) {
        String rule= Constant.AUDIT_ROLE;
        String msg=rule.formatted(reviewDTO.getContent());
        String res = chatClient.prompt()
                .system(p -> p.param("now", LocalDateTime.now()))
                .user(msg)
                .call()
                .content();
        res= Optional.ofNullable(res).orElse("no");
        if(res.contains("no")){
            log.info("审核失败");
            return false;
        }
        log.info("审核成功");
        return true;

    }

    @Override
    public Long startNewChat(Long userId) {
        Long sessionId=MIN_5_DIGIT + RANDOM.nextLong(MAX_5_DIGIT - MIN_5_DIGIT + 1);
        ChatConversation conversation = ChatConversation.builder()
                .id(sessionId).conversationType(3)
                .title(UUID.randomUUID().toString())
                .createUserId(userId)
                .groupId(0L)
                .targetUserId(0L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        conversationMapper.insert(conversation);
        return sessionId;

    }

    @Override
    public List<ChatMessageVO> listHistory(Long userId) {
        if(!Objects.equals(userId, UserContextHolder.getUserId())){
            throw new RuntimeException(ResultConstant.UNAUTHORIZED.getMsg());
        }
        LambdaQueryWrapper<ChatConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatConversation::getCreateUserId,userId);

        List<ChatConversation> chatMessages = chatConversationMapper.selectList(wrapper);
        return chatMessages.stream().map(c -> {
            ChatMessageVO chatMessageVO = new ChatMessageVO();
            chatMessageVO.setConversationId(c.getId());
            chatMessageVO.setContent(c.getTitle());
            return chatMessageVO;
        }).toList();
    }

    @Override
    public String stopSession(ChatStopDTO stopDTO) {
        String key=stopDTO.getUserId()+":"+stopDTO.getSessionId();
        GENERATE_STATUS.remove(key);
        return "停止会话成功";

    }

    @Override
    public String generateText(ChatTextDTO textDTO) {
        return null;
    }

    @Override
    public String polishText(ChatTextDTO textDTO) {
        return null;
    }

    @Override
    public boolean reviewPostV2(PostReviewDTO reviewDTO){
        //
        String content = reviewDTO.getContent();
        if(StrUtil.isBlank(content)){
            throw new RuntimeException("帖子审核内容为空");
        }
        List<String> first = SensitiveWordHelper.findAll(content);
        if(CollectionUtil.isNotEmpty(first)){
            return false;
        }
        content = TextNormalizeUtils.normalize(content);
        List<String> two =SensitiveWordHelper.findAll(content);
        if(CollectionUtil.isEmpty(two)){
            return true;
        }
        reviewDTO.setContent(content);
        //调用AI安全审核接口(暂时未接入阿里云安全内容审核接口）
        //用普通大模型提示词工程完成审核
        //TODO
        return reviewPostV1(reviewDTO);
    }


}
