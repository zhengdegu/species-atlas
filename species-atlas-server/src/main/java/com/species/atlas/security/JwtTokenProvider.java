package com.species.atlas.security;

import com.species.atlas.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 提供者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getExpiration());

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getRefreshExpiration());

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    /**
     * 获取 Token 类型（access/refresh）
     */
    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    /**
     * 验证 Token
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token 已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的 JWT Token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("无效的 JWT Token: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT 签名验证失败: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT Token 为空: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 验证是否是 Access Token
     */
    public boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    /**
     * 验证是否是 Refresh Token
     */
    public boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }

    /**
     * 获取 Access Token 过期时间（秒）
     */
    public long getAccessTokenExpirationSeconds() {
        return jwtConfig.getExpiration() / 1000;
    }

    /**
     * 解析 Token
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
