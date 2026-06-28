package com.company.task.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 当前登录用户上下文（线程本地变量，请求结束时由过滤器清理）。
 */
public class CurrentUser {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 登录用户信息（来自 JWT）。
     */
    public record LoginUser(Long userId, String username, String name, String role) {
        public boolean isMentor() {
            return "MENTOR".equals(role);
        }

        public static LoginUser fromClaims(Claims claims, JwtUtil jwtUtil) {
            return new LoginUser(
                    jwtUtil.getUserId(claims),
                    jwtUtil.getUsername(claims),
                    jwtUtil.getName(claims),
                    jwtUtil.getRole(claims)
            );
        }
    }

    /** 便捷：从 HttpServletRequest 获取（仅用于工具方法，主流程用 ThreadLocal） */
    public static String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
