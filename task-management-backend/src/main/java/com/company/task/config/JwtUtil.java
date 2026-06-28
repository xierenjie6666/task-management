package com.company.task.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：生成 / 解析 / 校验 Token。
 *
 * <p>AI 辅助说明：基于 jjwt 0.12.x API 实现。</p>
 */
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expire-hours}")
    private long expireHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT。subject=userId，自定义 claim 含 username/role/name。
     */
    public String generate(Long userId, String username, String name, String role) {
        long now = System.currentTimeMillis();
        long exp = now + expireHours * 3600_000L;
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("name", name)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(new Date(exp))
                .signWith(key())
                .compact();
    }

    /**
     * 解析 Token，返回 Claims；失败返回 null。
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    public String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    public String getName(Claims claims) {
        return claims.get("name", String.class);
    }
}
