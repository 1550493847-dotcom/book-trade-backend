package com.MySpringBoot.my_first_app.controler;

import com.MySpringBoot.my_first_app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/search")
public class SearchHistoryController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String HISTORY_PREFIX = "search:history:";
    private static final int MAX_HISTORY = 50;

    private Integer getUserId(HttpServletRequest request) {
        String token = jwtUtil.extractToken(request.getHeader("Authorization"));
        return token != null ? jwtUtil.getUserIdFromToken(token) : null;
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/history")
    public Map<String, Object> getHistory(HttpServletRequest request) {
        Map<String, Object> r = new HashMap<>();
        Integer userId = getUserId(request);
        if (userId == null) {
            r.put("code", 200);
            r.put("data", Collections.emptyList());
            return r;
        }
        String key = HISTORY_PREFIX + userId;
        List<Object> list = redisTemplate != null
            ? redisTemplate.opsForList().range(key, 0, 19)
            : Collections.emptyList();
        r.put("code", 200);
        r.put("data", list != null ? list : Collections.emptyList());
        return r;
    }

    @PostMapping("/history")
    public Map<String, Object> addHistory(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Map<String, Object> r = new HashMap<>();
        Integer userId = getUserId(request);
        String keyword = body.get("keyword");
        if (userId == null || keyword == null || keyword.trim().isEmpty()) {
            r.put("code", 200);
            return r;
        }
        keyword = keyword.trim();
        String key = HISTORY_PREFIX + userId;
        if (redisTemplate != null) {
            // 移除已有相同关键词（去重）
            redisTemplate.opsForList().remove(key, 0, keyword);
            // 插入到头部
            redisTemplate.opsForList().leftPush(key, keyword);
            // 截断到 MAX_HISTORY
            redisTemplate.opsForList().trim(key, 0, MAX_HISTORY - 1);
        }
        r.put("code", 200);
        return r;
    }

    @DeleteMapping("/history")
    public Map<String, Object> clearHistory(HttpServletRequest request) {
        Map<String, Object> r = new HashMap<>();
        Integer userId = getUserId(request);
        if (userId != null && redisTemplate != null) {
            redisTemplate.delete(HISTORY_PREFIX + userId);
        }
        r.put("code", 200);
        return r;
    }

    @DeleteMapping("/history/{keyword}")
    public Map<String, Object> deleteKeyword(@PathVariable String keyword, HttpServletRequest request) {
        Map<String, Object> r = new HashMap<>();
        Integer userId = getUserId(request);
        if (userId != null && redisTemplate != null) {
            redisTemplate.opsForList().remove(HISTORY_PREFIX + userId, 0, keyword);
        }
        r.put("code", 200);
        return r;
    }
}
