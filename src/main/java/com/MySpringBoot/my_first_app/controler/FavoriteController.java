package com.MySpringBoot.my_first_app.controler;

import com.MySpringBoot.my_first_app.service.FavoriteService;
import com.MySpringBoot.my_first_app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

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

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Map<String, Integer> params, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        Integer bookId = params.get("bookId");
        if (favoriteService.addFavorite(userId, bookId)) {
            response.put("code", 200);
            response.put("message", "收藏成功");
        } else {
            response.put("code", 400);
            response.put("message", "已收藏");
        }
        return response;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> remove(@PathVariable Integer id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        response.put("code", favoriteService.removeFavorite(id) ? 200 : 404);
        response.put("message", favoriteService.removeFavorite(id) ? "取消收藏成功" : "收藏不存在");
        return response;
    }

    @DeleteMapping("/book/{bookId}")
    public Map<String, Object> removeByBook(@PathVariable Integer bookId, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        favoriteService.removeByBook(userId, bookId);
        response.put("code", 200);
        response.put("message", "取消收藏成功");
        return response;
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
        response.put("data", favoriteService.getMyFavorites(userId));
        return response;
    }

    @GetMapping("/check/{bookId}")
    public Map<String, Object> check(@PathVariable Integer bookId, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        response.put("code", 200);
        response.put("data", favoriteService.checkFavorite(userId, bookId));
        return response;
    }
}
