package com.MySpringBoot.my_first_app.service;

import com.MySpringBoot.my_first_app.entity.ChatMessage;
import com.MySpringBoot.my_first_app.mapper.ChatMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    public boolean sendMessage(Integer fromUserId, Integer toUserId, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setContent(content);
        return chatMessageMapper.insert(msg) > 0;
    }

    public List<ChatMessage> getConversation(Integer userId, Integer otherId) {
        return chatMessageMapper.findConversation(userId, otherId);
    }

    public List<Map<String, Object>> getChatList(Integer userId) {
        List<ChatMessage> all = chatMessageMapper.findByUserId(userId);
        Map<Integer, ChatMessage> latest = new LinkedHashMap<>();
        for (ChatMessage msg : all) {
            Integer otherId = msg.getFromUserId().equals(userId) ? msg.getToUserId() : msg.getFromUserId();
            if (!latest.containsKey(otherId)) {
                latest.put(otherId, msg);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, ChatMessage> entry : latest.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("otherId", entry.getKey());
            item.put("lastMessage", entry.getValue().getContent());
            item.put("lastTime", entry.getValue().getCreateTime());
            result.add(item);
        }
        return result;
    }
}
