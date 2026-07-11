package com.MySpringBoot.my_first_app.service;

import com.MySpringBoot.my_first_app.entity.User;
import com.MySpringBoot.my_first_app.mapper.UserMapper;
import com.MySpringBoot.my_first_app.websocket.ChatWebSocketEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) return null;
        if (passwordEncoder.matches(password, user.getPassword())) return user;
        return null;
    }

    public boolean register(String username, String password) {
        User existUser = userMapper.findByUsername(username);
        if (existUser != null) return false;
        User user = new User();
        user.setUsername(username);
        user.setNickname(username);
        user.setPassword(passwordEncoder.encode(password));
        return userMapper.insert(user) > 0;
    }

    public User getUserById(Integer id) {
        return userMapper.findById(id);
    }

    public boolean updateUser(User user) {
        return userMapper.update(user) > 0;
    }

    public boolean updatePassword(Integer id, String newPassword) {
        return userMapper.updatePassword(id, passwordEncoder.encode(newPassword)) > 0;
    }

    public boolean updateLoginInfo(Integer id, LocalDateTime lastLoginTime, String lastLoginIp) {
        return userMapper.updateLoginInfo(id, lastLoginTime, lastLoginIp) > 0;
    }

    public boolean updateHeartbeat(Integer id) {
        // 同时刷新 Redis 在线状态
        try {
            ChatWebSocketEndpoint.refreshUserOnlineStatus(id);
        } catch (Exception ignored) {
        }
        return userMapper.updateHeartbeat(id) > 0;
    }
}
