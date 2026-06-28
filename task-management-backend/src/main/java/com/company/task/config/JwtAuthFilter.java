package com.company.task.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器：从 Authorization 头解析 Token，校验后写入 CurrentUser 上下文。
 *
 * <p>放行路径：登录/注册/资讯/静态资源；其余接口必须携带有效 Token。</p>
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // 放行：登录注册、资讯接口（公开）、OPTIONS 预检
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register")
                || path.startsWith("/api/news") || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String token = CurrentUser.extractToken(request);
        if (token != null) {
            Claims claims = jwtUtil.parse(token);
            if (claims != null) {
                CurrentUser.set(CurrentUser.LoginUser.fromClaims(claims, jwtUtil));
            }
        }

        // 仅对 /api/** 业务接口强制要求登录
        if (path.startsWith("/api/") && CurrentUser.get() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
            return;
        }

        try {
            chain.doFilter(request, response);
        } finally {
            CurrentUser.clear();
        }
    }
}
