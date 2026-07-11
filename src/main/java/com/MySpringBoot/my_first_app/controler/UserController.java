package com.MySpringBoot.my_first_app.controler;

import com.MySpringBoot.my_first_app.dto.LoginRequest;
import com.MySpringBoot.my_first_app.dto.LoginResponse;
import com.MySpringBoot.my_first_app.entity.User;
import com.MySpringBoot.my_first_app.service.UserService;
import com.MySpringBoot.my_first_app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private Integer getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return jwtUtil.getUserIdFromToken(jwtUtil.extractToken(token));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        Map<String, Object> response = new HashMap<>();
        User user = userService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            response.put("code", 401);
            response.put("message", "用户名或密码错误");
            return response;
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 记录登录时间和IP
        String ip = getClientIp(servletRequest);
        userService.updateLoginInfo(user.getId(), LocalDateTime.now(), ip);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole("user");
        loginResponse.setUserInfo(userInfo);

        response.put("code", 200);
        response.put("data", loginResponse);
        return response;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        boolean success = userService.register(request.getUsername(), request.getPassword());
        if (success) {
            response.put("code", 200);
            response.put("message", "注册成功");
        } else {
            response.put("code", 400);
            response.put("message", "用户名已存在");
        }
        return response;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        String token = jwtUtil.extractToken(request.getHeader("Authorization"));
        if (token != null) {
            jwtUtil.blacklistToken(token);
        }
        response.put("code", 200);
        response.put("message", "退出成功");
        return response;
    }

    @GetMapping("/info")
    public Map<String, Object> getInfo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        User user = userService.getUserById(userId);
        if (user == null) {
            response.put("code", 404);
            response.put("message", "用户不存在");
            return response;
        }
        // 不返回密码
        user.setPassword(null);
        response.put("code", 200);
        response.put("data", user);
        return response;
    }

    @PutMapping("/info")
    public Map<String, Object> updateInfo(@RequestBody User user, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        user.setId(userId);
        if (userService.updateUser(user)) {
            response.put("code", 200);
            response.put("message", "更新成功");
        } else {
            response.put("code", 500);
            response.put("message", "更新失败");
        }
        return response;
    }

    @PutMapping("/password")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        String newPassword = params.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            response.put("code", 400);
            response.put("message", "密码长度至少6位");
            return response;
        }
        if (userService.updatePassword(userId, newPassword)) {
            response.put("code", 200);
            response.put("message", "修改成功");
        } else {
            response.put("code", 500);
            response.put("message", "修改失败");
        }
        return response;
    }

    @PostMapping("/heartbeat")
    public Map<String, Object> heartbeat(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        userService.updateHeartbeat(userId);
        response.put("code", 200);
        response.put("message", "ok");
        return response;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        User user = userService.getUserById(id);
        if (user == null) {
            response.put("code", 404);
            response.put("message", "用户不存在");
            return response;
        }
        // 只返回必要信息
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("nickname", user.getNickname());
        info.put("avatar", user.getAvatar());
        info.put("createTime", user.getCreateTime());
        info.put("lastLoginTime", user.getLastLoginTime());
        info.put("lastLoginIp", user.getLastLoginIp());
        response.put("code", 200);
        response.put("data", info);
        return response;
    }
}
