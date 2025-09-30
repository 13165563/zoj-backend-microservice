package com.zluolan.zojbackendcommon.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 简化的JWT工具类 - 使用环境变量存储密钥
 */
@Slf4j
public class SimpleJwtTool {
    
    private javax.crypto.SecretKey secretKey;
    
    public SimpleJwtTool() {
        init();
    }
    
    public void init() {
        // 从环境变量获取密钥，如果没有则使用默认密钥
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isEmpty()) {
            secret = "zoj-backend-jwt-secret-key-for-development-only-change-in-production";
            log.warn("使用默认JWT密钥，生产环境请设置JWT_SECRET环境变量");
        }
        
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT工具初始化完成");
    }
    
    /**
     * 生成JWT令牌
     */
    public String generateToken(Long userId, String userAccount, String userRole) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userAccount", userAccount);
        claims.put("userRole", userRole);
        
        return generateToken(claims);
    }
    
    /**
     * 生成JWT令牌
     */
    public String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 24 * 60 * 60 * 1000); // 24小时过期
        
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * 解析JWT令牌
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT令牌已过期: {}", e.getMessage());
            throw new RuntimeException("令牌已过期");
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT令牌: {}", e.getMessage());
            throw new RuntimeException("不支持的令牌格式");
        } catch (MalformedJwtException e) {
            log.warn("JWT令牌格式错误: {}", e.getMessage());
            throw new RuntimeException("令牌格式错误");
        } catch (SecurityException e) {
            log.warn("JWT令牌签名验证失败: {}", e.getMessage());
            throw new RuntimeException("令牌签名验证失败");
        } catch (IllegalArgumentException e) {
            log.warn("JWT令牌参数错误: {}", e.getMessage());
            throw new RuntimeException("令牌参数错误");
        }
    }
    
    /**
     * 验证JWT令牌
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return null;
    }
    
    /**
     * 从令牌中获取用户账号
     */
    public String getUserAccountFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userAccount", String.class);
    }
    
    /**
     * 从令牌中获取用户角色
     */
    public String getUserRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userRole", String.class);
    }
}
