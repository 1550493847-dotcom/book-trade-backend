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
 * 每 IP 每分钟最多 120 次请求
 */
@Component
public class RateLimitingFilter implements Filter {

    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    private static final int MAX_REQUESTS_PER_MINUTE = 120;
    private static final long WINDOW_SECONDS = 60;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 只对 /api/ 进行限流
        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        if (redisTemplate != null) {
            String ip = getClientIp(request);
            String key = RATE_LIMIT_PREFIX + ip + ":" + path;
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == 1) {
                // 第一次访问，设置过期时间
                redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            if (count != null && count > MAX_REQUESTS_PER_MINUTE) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(429);
                response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
                return;
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
