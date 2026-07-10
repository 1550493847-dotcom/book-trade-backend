package com.MySpringBoot.my_first_app.controler;

import com.MySpringBoot.my_first_app.service.NotificationService;
import com.MySpringBoot.my_first_app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    private Integer getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }

    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(defaultValue = "all") String type, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        response.put("code", 200);
        response.put("data", notificationService.getNotifications(userId, type));
        return response;
    }

    @PutMapping("/{id}/read")
    public Map<String, Object> markRead(@PathVariable Integer id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        response.put("code", notificationService.markRead(id) ? 200 : 404);
        response.put("message", notificationService.markRead(id) ? "已读" : "通知不存在");
        return response;
    }
}
