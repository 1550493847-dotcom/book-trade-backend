package com.MySpringBoot.my_first_app.service;

import com.MySpringBoot.my_first_app.entity.Notification;
import com.MySpringBoot.my_first_app.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private static final String NOTIFICATION_QUEUE_KEY = "notification:queue";

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public List<Notification> getNotifications(Integer userId, String type) {
        if (type == null || type.isEmpty() || "all".equals(type)) {
            return notificationMapper.findByUserId(userId);
        }
        return notificationMapper.findByUserIdAndType(userId, type);
    }

    public boolean markRead(Integer id) {
        return notificationMapper.markRead(id) > 0;
    }

    public void createNotification(Integer userId, String type, String title, String content) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        notificationMapper.insert(n);

        // 异步通知队列：将通知 ID 推入 Redis List
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForList().leftPush(NOTIFICATION_QUEUE_KEY, String.valueOf(n.getId()));
            } catch (Exception ignored) {
            }
        }
    }
}
