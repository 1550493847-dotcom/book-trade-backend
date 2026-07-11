package com.MySpringBoot.my_first_app.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {
    private static final String SECRET = System.getenv().getOrDefault("JWT_SECRET", "change-me-in-production");
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000; // 7天

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    // 生成 token（带 jti）
    public String generateToken(Integer userId, String username) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRE_TIME);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setId(jti)
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    // 验证 token
    public Claims parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();

            // 检查是否在黑名单中
            if (redisTemplate != null) {
                String jti = claims.getId();
                if (jti != null) {
                    Boolean isBlacklisted = redisTemplate.hasKey(BLACKLIST_PREFIX + jti);
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        return null; // token 已被拉黑
                    }
                }
            }

            return claims;
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return Integer.parseInt(claims.getSubject());
        }
        return null;
    }

    // 将 token 加入黑名单（退出登录时调用）
    public void blacklistToken(String token) {
        if (redisTemplate == null) return;
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
            String jti = claims.getId();
            if (jti != null) {
                // 计算剩余有效期
                long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    redisTemplate.opsForValue().set(
                            BLACKLIST_PREFIX + jti, "1", ttl, TimeUnit.MILLISECONDS);
                }
            }
        } catch (Exception ignored) {
        }
    }

    // 从请求头中提取 Bearer token
    public String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
