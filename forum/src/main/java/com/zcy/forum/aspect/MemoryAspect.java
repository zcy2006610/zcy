package com.zcy.forum.aspect;
import com.zcy.forum.annotation.SessionMemory;
import com.zcy.forum.domain.dto.ChatDTO;
import com.zcy.forum.domain.entity.ChatMessage;
import com.zcy.forum.mapper.ChatMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Aspect
@Component
@Slf4j
public class MemoryAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ThreadPoolTaskExecutor asyncTaskExecutor;

    // 定义切点：匹配带有SessionMemory注解的方法
    @Pointcut("@annotation(com.zcy.forum.annotation.SessionMemory)")
    public void memoryPointcut() {}

    @Around("memoryPointcut()")
    public Object saveSessionMemory2Redis(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取方法签名和注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SessionMemory annotation = method.getAnnotation(SessionMemory.class);

        // 如果注解标记为不需要会话记忆，直接执行原方法
        if(!annotation.required()){
            return joinPoint.proceed();
        }

        // 2. 获取方法入参中的ChatDTO对象（假设第一个参数是ChatDTO）
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0 || !(args[0] instanceof ChatDTO chatDTO)) {
            log.warn("会话记忆切面：方法入参不存在ChatDTO对象，跳过记忆处理");
            return joinPoint.proceed();
        }

        Long userId = chatDTO.getUserId();
        String userMessage = chatDTO.getMessage();
        Long currentSessionId = chatDTO.getCurrentSessionId();
        // 校验关键参数，避免空指针
        if (userId == null ||currentSessionId==null|| userMessage == null || userMessage.trim().isEmpty()) {
            log.warn("会话记忆切面：userId或用户消息为空，跳过记忆处理");
            return joinPoint.proceed();
        }
        String redisKey = "session:memory:" + userId+":"+currentSessionId;

        List<String> history = new ArrayList<>();

        try {
            // 3. 从Redis获取历史会话记录（不存在则返回空列表）
            history = redisTemplate.opsForList().range(redisKey, 0, -1);
            if (history == null) {
                history = new ArrayList<>();
            }

            // 4. 将当前用户消息添加到历史记录（先不存入Redis，等AI回复后一起更新）
            history.add("用户: " + userMessage);

            // 5. 限制历史记录长度（最多保留20轮对话，避免数据过大）
            if (history.size() > 20) {
                history = history.subList(history.size() - 20, history.size());
            }

            // 6. 重构ChatDTO的message：拼接历史会话 + 当前消息（供AI接口使用）
            // 修复原代码重复拼接userMessage的问题
            String oldMessage= chatDTO.getMessage();
            chatDTO.setMessage(String.join("\n", history));

            // 7. 执行原始方法，获取AI回复结果
            Object result = joinPoint.proceed(args); // 传递修改后的参数

            // 8. 将AI回复添加到历史记录
            if (result != null && !result.toString().trim().isEmpty()) {
                history.add("AI: " + result.toString());
                // 再次校验长度，避免添加AI回复后超出限制
                if (history.size() > 20) {
                    history = history.subList(history.size() - 20, history.size());
                }
            }
            //存储会话到数据库
            CompletableFuture.runAsync(() -> {
                try {
                    String content="用户: "+oldMessage+"AI回复: "+Optional.ofNullable(Objects.requireNonNull(result).toString())
                            .orElse("");
                    ChatMessage chatMessage = ChatMessage.builder()
                            .conversationId(currentSessionId)
                            .senderId(userId)
                            .receiverId(0L)
                            .content(content)
                            .messageType(1) // 1-文本消息
                            .messageStatus(1) // 1-已发送（修复状态错误）
                            .aiMemoryKey(redisKey)
                            .isDeleted(0)
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .build();
                    chatMessageMapper.insert(chatMessage);
                    log.info("会话历史异步入库成功：sessionId={}, userId={}", currentSessionId, userId);
                } catch (Exception e) {
                    log.error("会话历史异步入库失败：sessionId={}, userId={}", currentSessionId, userId, e);
                    // 可选：重试逻辑（最多3次）
                }
            }, asyncTaskExecutor); // 使用自定义线程池
            // 9. 使用Redis管道批量更新（清空旧数据 + 写入新数据 + 设置过期时间）
            List<String> finalHistory = history;
            // 8. Redis更新：追加而非覆盖（修复数据丢失问题）
            redisTemplate.executePipelined((RedisCallback<?>) connection -> {
                // 先清理超出长度的旧记录（保留最新20条）
                long currentSize = Optional.ofNullable(redisTemplate.opsForList().size(redisKey)).orElseThrow();

                if (currentSize > 19) { // 加上新消息后不超过20
                    connection.lTrim(redisKey.getBytes(), currentSize - 19, -1);
                }
                // 右插入新消息（保证顺序：旧→新）
                connection.rPush(redisKey.getBytes(), ("用户: " + userMessage).getBytes());
                if (result != null && !result.toString().trim().isEmpty()) {
                    connection.rPush(redisKey.getBytes(), ("AI: " + result.toString()).getBytes());
                }
                // 设置过期时间（24小时）
                connection.expire(redisKey.getBytes(), Duration.ofHours(24).getSeconds());
                return null;
            });

            log.info("会话记忆切面：用户{}的会话记录已更新到Redis，当前记录数：{}", userId, history.size());
            return result;
        } catch (Exception e) {
            log.error("会话记忆切面处理异常：", e);
            // 即使Redis操作失败，也不影响核心业务执行
            return joinPoint.proceed();
        }
    }
}