package com.MySpringBoot.my_first_app.websocket;

import com.MySpringBoot.my_first_app.config.SpringContextUtil;
import com.MySpringBoot.my_first_app.entity.ChatMessage;
import com.MySpringBoot.my_first_app.mapper.ChatMessageMapper;
import com.MySpringBoot.my_first_app.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@ServerEndpoint(value = "/ws/chat")
public class ChatWebSocketEndpoint {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketEndpoint.class);

    // 本地在线用户缓存（当前实例的连接）
    private static final Map<Integer, Session> localOnlineUsers = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    // Redis keys
    private static final String ONLINE_PREFIX = "online:user:";
    private static final long ONLINE_TTL_SECONDS = 30;
    private static final String CHAT_CHANNEL = "chat:channel";

    // Spring beans (lazy init via SpringContextUtil)
    private static JwtUtil jwtUtil;
    private static ChatMessageMapper chatMessageMapper;
    private static RedisTemplate<String, Object> redisTemplate;
    private static RedisMessageSubscriber messageSubscriber;
    private static RedisMessageListenerContainer listenerContainer;

    private static boolean subscriberInitialized = false;

    private JwtUtil getJwtUtil() {
        if (jwtUtil == null) {
            jwtUtil = SpringContextUtil.getBean(JwtUtil.class);
        }
        return jwtUtil;
    }

    private ChatMessageMapper getChatMessageMapper() {
        if (chatMessageMapper == null) {
            chatMessageMapper = SpringContextUtil.getBean(ChatMessageMapper.class);
        }
        return chatMessageMapper;
    }

    @SuppressWarnings("unchecked")
    private RedisTemplate<String, Object> getRedisTemplate() {
        if (redisTemplate == null) {
            try {
                redisTemplate = SpringContextUtil.getBean(RedisTemplate.class);
            } catch (Exception e) {
                return null;
            }
        }
        return redisTemplate;
    }

    private synchronized void ensureSubscriber() {
        if (subscriberInitialized) return;
        try {
            RedisMessageListenerContainer container = SpringContextUtil.getBean(RedisMessageListenerContainer.class);
            messageSubscriber = new RedisMessageSubscriber();
            container.addMessageListener(messageSubscriber, new ChannelTopic(CHAT_CHANNEL));
            subscriberInitialized = true;
            log.info("Redis Pub/Sub 订阅者已初始化");
        } catch (Exception e) {
            log.warn("Redis 不可用，Pub/Sub 未启动: {}", e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        String query = session.getQueryString();
        Integer userId = null;
        if (query != null && query.startsWith("token=")) {
            String tk = query.substring(6);
            userId = getJwtUtil().getUserIdFromToken(tk);
        }
        if (userId == null) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Auth failed"));
            } catch (IOException e) {
                log.error("Close error", e);
            }
            return;
        }

        // 存入本地
        localOnlineUsers.put(userId, session);
        log.info("用户 {} 已连接（本地连接），当前本地在线: {}", userId, localOnlineUsers.size());

        // 写入 Redis 在线状态
        RedisTemplate<String, Object> rt = getRedisTemplate();
        if (rt != null) {
            rt.opsForValue().set(ONLINE_PREFIX + userId, "connected", ONLINE_TTL_SECONDS, TimeUnit.SECONDS);
            // 初始化 Pub/Sub 订阅者（只需一次）
            ensureSubscriber();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> msg = mapper.readValue(message, Map.class);

            Integer fromUserId = getUserIdBySession(session);
            if (fromUserId == null) return;

            Object receiverObj = msg.get("receiverId");
            if (receiverObj == null) receiverObj = msg.get("toUserId");
            if (receiverObj == null) return;
            Integer toUserId = Integer.valueOf(receiverObj.toString());

            String content = (String) msg.get("content");
            if (content == null || content.trim().isEmpty()) return;

            // 保存到数据库
            ChatMessage chatMsg = new ChatMessage();
            chatMsg.setFromUserId(fromUserId);
            chatMsg.setToUserId(toUserId);
            chatMsg.setContent(content.trim());
            getChatMessageMapper().insert(chatMsg);

            // 构建响应
            Map<String, Object> response = new java.util.LinkedHashMap<>();
            response.put("type", "message");
            Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("id", chatMsg.getId());
            data.put("senderId", fromUserId);
            data.put("receiverId", toUserId);
            data.put("content", chatMsg.getContent());
            data.put("createTime", chatMsg.getCreateTime() != null ? chatMsg.getCreateTime().toString() : null);
            response.put("data", data);
            String json = mapper.writeValueAsString(response);

            // 先尝试本地转发
            boolean locallyDelivered = forwardToLocalUser(toUserId, json);

            // 无论本地是否送达，都通过 Redis Pub/Sub 广播（跨实例）
            RedisTemplate<String, Object> rt = getRedisTemplate();
            if (rt != null) {
                // 包装消息，带上 receiverId 方便订阅者过滤
                Map<String, Object> pubMsg = new java.util.LinkedHashMap<>();
                pubMsg.put("type", "message");
                pubMsg.put("receiverId", toUserId);
                pubMsg.put("payload", json);
                rt.convertAndSend(CHAT_CHANNEL, mapper.writeValueAsString(pubMsg));
            }

            // 如果接收者不在线（本地 + Redis 都不在线），创建通知
            if (!locallyDelivered && !isUserOnline(toUserId)) {
                try {
                    createNotification(toUserId, fromUserId, content);
                } catch (Exception ignored) {
                }
            }

            // 回传给发送者
            if (session.isOpen()) {
                session.getBasicRemote().sendText(json);
            }

        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        Integer userId = getUserIdBySession(session);
        if (userId != null) {
            localOnlineUsers.remove(userId);
            log.info("用户 {} 已断开，当前本地在线: {}", userId, localOnlineUsers.size());

            // Redis 在线状态由心跳维持，断开连接不立即删除
            // 心跳超时后 Redis key 自动过期
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 错误: {}", error.getMessage());
    }

    // ==================== 辅助方法 ====================

    private Integer getUserIdBySession(Session session) {
        for (Map.Entry<Integer, Session> entry : localOnlineUsers.entrySet()) {
            if (entry.getValue().equals(session)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 转发消息给本地连接的用户
     * @return true 如果成功送达
     */
    private boolean forwardToLocalUser(Integer userId, String json) {
        Session receiverSession = localOnlineUsers.get(userId);
        if (receiverSession != null && receiverSession.isOpen()) {
            try {
                receiverSession.getBasicRemote().sendText(json);
                return true;
            } catch (IOException e) {
                log.warn("转发消息给用户 {} 失败", userId, e);
                localOnlineUsers.remove(userId);
            }
        }
        return false;
    }

    /**
     * 检查用户是否在线（Redis 中是否存在）
     */
    public static boolean isUserOnline(Integer userId) {
        try {
            RedisTemplate<String, Object> rt = SpringContextUtil.getBean(RedisTemplate.class);
            Boolean hasKey = rt.hasKey(ONLINE_PREFIX + userId);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 静态转发方法（供 RedisMessageSubscriber 调用）
     */
    public static boolean forwardToLocalUserStatic(Integer userId, String json) {
        Session receiverSession = localOnlineUsers.get(userId);
        if (receiverSession != null && receiverSession.isOpen()) {
            try {
                receiverSession.getBasicRemote().sendText(json);
                return true;
            } catch (IOException e) {
                log.warn("[Pub/Sub] 转发消息给用户 {} 失败", userId, e);
                localOnlineUsers.remove(userId);
            }
        }
        return false;
    }

    /**
     * 刷新用户在线状态（由心跳调用）
     */
    public static void refreshUserOnlineStatus(Integer userId) {
        try {
            RedisTemplate<String, Object> rt = SpringContextUtil.getBean(RedisTemplate.class);
            rt.opsForValue().set(ONLINE_PREFIX + userId, "connected", ONLINE_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取本地在线用户数量
     */
    public static int getLocalOnlineCount() {
        return localOnlineUsers.size();
    }

    private void createNotification(Integer toUserId, Integer fromUserId, String content) {
        try {
            com.MySpringBoot.my_first_app.service.NotificationService ns =
                    SpringContextUtil.getBean(com.MySpringBoot.my_first_app.service.NotificationService.class);
            ns.createNotification(toUserId, "transaction",
                    "您收到一条新消息",
                    "用户 " + fromUserId + " 给您发送了一条消息: " +
                            (content.length() > 50 ? content.substring(0, 50) + "..." : content));
        } catch (Exception e) {
            log.warn("创建通知失败", e);
        }
    }
}
