package com.MySpringBoot.my_first_app.utils;

// utils/JwtUtil.java
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET = "your-secret-key-please-change-this";
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000; // 7天

    // 生成 token
    public String generateToken(Integer userId, String username) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRE_TIME);

        return Jwts.builder()
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
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
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
}