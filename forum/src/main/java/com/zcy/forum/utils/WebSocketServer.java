package com.zcy.forum.utils;

import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
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

    @Resource
    private OnlineStatusUtils onlineStatusUtils;

    /**
     * 连接建立成功
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
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
    public void onMessage(String message, Session session) {
        log.info("收到客户端消息：{}", message);
        // 心跳包：刷新在线状态过期时间
        if ("heartbeat".equals(message)) {
            onlineStatusUtils.userOnline(Long.parseLong(session.getId()), session.getId());
        }
    }

    // ====================== 核心：主动推送消息给用户 ======================
    public void sendMessageToUser(Long userId, String message) {
        try {
            // 1. 判断用户是否在线
            if (!onlineStatusUtils.isOnline(userId)) {
                log.info("用户【{}】不在线，消息存入数据库", userId);
                return;
            }

            // 2. 获取会话
            String sessionId = onlineStatusUtils.getSessionId(userId);
            Session session = SESSION_POOL.get(sessionId);
            if (session == null || !session.isOpen()) {
                log.info("用户【{}】会话已关闭", userId);
                onlineStatusUtils.userOffline(userId);
                return;
            }

            // 3. 异步推送消息
            session.getAsyncRemote().sendText(message);
            log.info("推送消息给用户【{}】成功：{}", userId, message);
        } catch (Exception e) {
            log.error("推送消息给用户【{}】失败：", userId, e);
        }
    }
}