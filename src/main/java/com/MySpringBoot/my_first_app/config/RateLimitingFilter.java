package com.MySpringBoot.my_first_app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 限流过滤器 — 基于 Redis 的滑动窗口算法
 * 普通接口: 每 IP 每分钟最多 120 次
 * 登录接口: 每 IP 每分钟最多 10 次
 */
@Component
public class RateLimitingFilter implements Filter {

    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    private static final int MAX_REQUESTS_PER_MINUTE = 120;
    private static final int MAX_LOGIN_PER_MINUTE = 10;
    private static final long WINDOW_SECONDS = 60;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        if (redisTemplate != null) {
            String ip = getClientIp(request);
            // 登录接口更严格的限流
            boolean isLogin = path.equals("/api/user/login");
            int maxRequests = isLogin ? MAX_LOGIN_PER_MINUTE : MAX_REQUESTS_PER_MINUTE;

            String key = RATE_LIMIT_PREFIX + ip + ":" + (isLogin ? "login" : path);
            try {
                Long count = redisTemplate.opsForValue().increment(key);
                if (count == 1) {
                    redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
                }
                if (count != null && count > maxRequests) {
                    String msg = isLogin
                        ? "{\"code\":429,\"message\":\"登录尝试过于频繁，请 1 分钟后再试\"}"
                        : "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}";
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(429);
                    response.getWriter().write(msg);
                    return;
                }
            } catch (Exception e) {
                // Redis 不可用时降级，不限制请求
            }
        }

        chain.doFilter(request, response);
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
}
