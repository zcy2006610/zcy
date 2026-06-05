package com.zcy.forum.server;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zcy.forum.config.RabbitMqConfig;
import com.zcy.forum.constant.MessageConstant;
import com.zcy.forum.domain.dto.UserMessageDTO;
import com.zcy.forum.domain.entity.ConversationMessage;
import com.zcy.forum.domain.entity.UserConversation;
import com.zcy.forum.utils.OnlineStatusUtils;
import com.zcy.forum.utils.RabbitMqUtil;
import com.zcy.forum.utils.SpringContextUtil;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 核心服务
 * 连接地址：ws://localhost:8080/ws/{userId}
 */
@Slf4j
@Component
@ServerEndpoint("/ws/{userId}")
public class WebSocketServer {

    // 本地缓存：会话ID -> Session（分布式用Redis）
    private static final Map<String, Session> SESSION_POOL = new ConcurrentHashMap<>();

    private static OnlineStatusUtils onlineStatusUtils;
    private static StringRedisTemplate stringRedisTemplate;
    private static RabbitMqUtil mqUtil;
    private static void initBeans() {
        if (onlineStatusUtils == null) {
            onlineStatusUtils = SpringContextUtil.getBean(OnlineStatusUtils.class);
        }
        if (stringRedisTemplate == null) {
            stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        }
        if (mqUtil == null) {
            mqUtil = SpringContextUtil.getBean(RabbitMqUtil.class);
        }
    }
    /**
     * 连接建立成功
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        initBeans();
        String sessionId = session.getId();
        // 保存会话
        SESSION_POOL.put(sessionId, session);
        // Redis 标记在线
        onlineStatusUtils.userOnline(userId, sessionId);
        log.info("用户【{}】连接WebSocket，会话ID：{}", userId, sessionId);
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose(@PathParam("userId") Long userId, Session session) {
        initBeans();
        String sessionId = session.getId();
        SESSION_POOL.remove(sessionId);
        onlineStatusUtils.userOffline(userId);
        log.info("用户【{}】断开WebSocket", userId);
    }

    /**
     * 发生错误
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误：", error);
    }

    /**
     * 接收客户端消息（心跳/业务消息）
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") Long userId, Session session) {
        initBeans();
        log.info("收到客户端消息：{}，来自用户：{}", message, userId);

        // 心跳处理
        if ("heartbeat".equals(message)) {
            onlineStatusUtils.userOnline(userId, session.getId());
            return;
        }

        // 校验
        if (userId == null || StrUtil.isBlank(message)) {
            return;
        }

        UserMessageDTO messageDTO = JSONUtil.toBean(message, UserMessageDTO.class);
        Long toUserId = messageDTO.getToUserId();
        if (toUserId == null) return;

        String convId = String.valueOf(messageDTO.getConversationId());
        long nowTime = System.currentTimeMillis();

        // ==========================
        // 消息实体（双方共用一条消息内容）
        // ==========================
        ConversationMessage msg = new ConversationMessage();
        msg.setMsgId(UUID.randomUUID().toString());
        BeanUtil.copyProperties(messageDTO, msg);
        msg.setSenderId(userId);
        msg.setReceiverId(toUserId);
        msg.setCreateTime(LocalDateTime.now());
        msg.setStatus(0);
        msg.setConvType(1);
        String jsonMsg = JSONUtil.toJsonStr(msg);

        // ==========================
        // 【关键】构建两个会话对象
        // ==========================

        // 1. 发送者自己的会话（A 看 B）
        UserConversation senderConv = UserConversation.builder()
                .conversationId(convId)
                .userId(userId)         // 自己是A
                .targetId(toUserId)     // 对方是B
                .convType(msg.getConvType())
                .isDeleted(0)
                .lastContent(msg.getContent())
                .updateTime(LocalDateTime.now())
                .build();

        // 2. 接收者的会话（B 看 A）
        UserConversation receiverConv = UserConversation.builder()
                .conversationId(convId)
                .userId(toUserId)       // 自己是B
                .targetId(userId)       // 对方是A
                .convType(msg.getConvType())
                .isDeleted(0)
                .lastContent(msg.getContent())
                .updateTime(LocalDateTime.now())
                .build();

        String senderConvJson = JSONUtil.toJsonStr(senderConv);
        String receiverConvJson = JSONUtil.toJsonStr(receiverConv);

        // ==========================
        // 在线推送（推给接收者）
        // ==========================
        if (onlineStatusUtils.isOnline(toUserId)) {
            String tousersessionId = onlineStatusUtils.getSessionId(toUserId);
            Session targetSession = SESSION_POOL.get(tousersessionId);
            if (targetSession != null && targetSession.isOpen()) {
                messageDTO.setUserId(userId);
                targetSession.getAsyncRemote().sendText(JSONUtil.toJsonStr(messageDTO));
            }
        }

        // ==========================
        // 【核心】双方都存 Redis
        // ==========================
        String senderKey = userId + ":" + convId;
        String receiverKey = toUserId + ":" + convId;
        String statusKey="msg:status:"+userId+":"+msg.getMsgId();
        // ------------------------------
        // 发送者存储（A）
        // ------------------------------
        stringRedisTemplate.opsForZSet().add(MessageConstant.SINGLE_MSG_LIST_PREFIX + senderKey, jsonMsg, nowTime);
        stringRedisTemplate.opsForZSet().add(MessageConstant.CONVERSATION_LIST_PREFIX + userId, senderConvJson, nowTime);

        // ------------------------------
        // 接收者存储（B）+ 未读+1
        // ------------------------------
        stringRedisTemplate.opsForZSet().add(MessageConstant.SINGLE_MSG_LIST_PREFIX + receiverKey, jsonMsg, nowTime);
        stringRedisTemplate.opsForZSet().add(MessageConstant.CONVERSATION_LIST_PREFIX + toUserId, receiverConvJson, nowTime);
        stringRedisTemplate.opsForValue().set(senderKey,"0");

        // 未读只给接收者 +1
        String unreadKey = MessageConstant.CONVERSATION_UNREAD_PREFIX + receiverKey;
        long unread = StrUtil.isNotBlank(stringRedisTemplate.opsForValue().get(unreadKey)) ? Long.parseLong(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(unreadKey))) : 0L;
        stringRedisTemplate.opsForValue().set(unreadKey, String.valueOf(unread + 1));

        // 异步入库
        mqUtil.sendMsg(RabbitMqConfig.MESSAGE_KEY, jsonMsg);
    }

    // ====================== 核心：主动推送消息给用户 ======================

}