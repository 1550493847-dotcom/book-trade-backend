package com.MySpringBoot.my_first_app.config;

import com.MySpringBoot.my_first_app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 认证过滤器 — 验证请求头中的 Bearer token 黑名单状态
 * 只对 /api/ 开头的请求生效
 */
@Component
public class JwtAuthFilter implements Filter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getRequestURI();

        // 只拦截 /api/ 路径
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        // 登录、注册、公开接口放行
        if (path.equals("/api/user/login") || path.equals("/api/user/register")) {
            chain.doFilter(request, response);
            return;
        }

        // GET 请求的公开接口放行（图书列表、详情、用户信息）
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            if (path.equals("/api/book/list") || path.matches("/api/book/\\d+")
                    || path.matches("/api/user/\\d+") || path.equals("/api/favorite/check/\\d+")) {
                chain.doFilter(request, response);
                return;
            }
        }

        // 检查 token
        String authHeader = request.getHeader("Authorization");
        String token = jwtUtil.extractToken(authHeader);

        if (token == null || jwtUtil.parseToken(token) == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
