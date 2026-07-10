package com.MySpringBoot.my_first_app.websocket;

import com.MySpringBoot.my_first_app.config.SpringContextUtil;
import com.MySpringBoot.my_first_app.entity.ChatMessage;
import com.MySpringBoot.my_first_app.mapper.ChatMessageMapper;
import com.MySpringBoot.my_first_app.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/ws/chat")
public class ChatWebSocketEndpoint {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketEndpoint.class);

    private static final Map<Integer, Session> onlineUsers = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    // 通过 SpringContextUtil 获取 Spring Bean
    private static JwtUtil jwtUtil;
    private static ChatMessageMapper chatMessageMapper;

    static {
        // 静态块中初始化（在第一次 WebSocket 连接时执行）
    }

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
        onlineUsers.put(userId, session);
        log.info("用户 {} 已连接，当前在线: {}", userId, onlineUsers.size());
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

            // 转发给接收者（在线）
            Session receiverSession = onlineUsers.get(toUserId);
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.getBasicRemote().sendText(json);
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
            onlineUsers.remove(userId);
            log.info("用户 {} 已断开，当前在线: {}", userId, onlineUsers.size());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 错误: {}", error.getMessage());
    }

    private Integer getUserIdBySession(Session session) {
        for (Map.Entry<Integer, Session> entry : onlineUsers.entrySet()) {
            if (entry.getValue().equals(session)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
