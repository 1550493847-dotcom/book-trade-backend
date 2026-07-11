package com.MySpringBoot.my_first_app.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Redis Pub/Sub 消息订阅者
 * 接收来自其他实例广播的消息，转发给本地 WebSocket 连接的用户
 */
public class RedisMessageSubscriber implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisMessageSubscriber.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> msg = mapper.readValue(body, Map.class);

            String type = (String) msg.get("type");
            if (!"message".equals(type)) return;

            // 获取目标用户
            Object receiverIdObj = msg.get("receiverId");
            if (receiverIdObj == null) return;
            Integer receiverId = Integer.valueOf(receiverIdObj.toString());

            // 获取消息体
            String payload = (String) msg.get("payload");
            if (payload == null) return;

            // 只转发给当前实例本地在线的用户
            // 避免重复转发（发送者实例已经转发过）
            boolean delivered = ChatWebSocketEndpoint.forwardToLocalUserStatic(receiverId, payload);
            if (delivered) {
                log.debug("Pub/Sub 消息已转发给本地用户 {}", receiverId);
            }
        } catch (Exception e) {
            log.warn("Pub/Sub 消息处理失败: {}", e.getMessage());
        }
    }
}
