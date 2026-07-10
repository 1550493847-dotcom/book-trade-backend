package com.MySpringBoot.my_first_app.controler;

import com.MySpringBoot.my_first_app.service.ChatService;
import com.MySpringBoot.my_first_app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

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
    public Map<String, Object> list(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        response.put("code", 200);
        response.put("data", chatService.getChatList(userId));
        return response;
    }

    @GetMapping("/messages/{otherId}")
    public Map<String, Object> messages(@PathVariable Integer otherId, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        response.put("code", 200);
        response.put("data", chatService.getConversation(userId, otherId));
        return response;
    }

    @PostMapping("/send")
    public Map<String, Object> send(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        // 兼容前端字段名 receiverId 和 toUserId
        Object receiverObj = params.get("receiverId");
        if (receiverObj == null) receiverObj = params.get("toUserId");
        if (receiverObj == null) {
            response.put("code", 400);
            response.put("message", "缺少 receiverId");
            return response;
        }
        Integer toUserId = Integer.valueOf(receiverObj.toString());
        Object contentObj = params.get("content");
        if (contentObj == null) {
            response.put("code", 400);
            response.put("message", "缺少 content");
            return response;
        }
        String content = contentObj.toString();
        if (chatService.sendMessage(userId, toUserId, content)) {
            response.put("code", 200);
            response.put("message", "发送成功");
        } else {
            response.put("code", 500);
            response.put("message", "发送失败");
        }
        return response;
    }
}
