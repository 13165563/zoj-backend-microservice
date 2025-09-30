package com.zluolan.zojbackendgateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zluolan.zojbackendcommon.common.ErrorCode;
import com.zluolan.zojbackendcommon.common.ResultUtils;
import com.zluolan.zojbackendcommon.exception.BusinessException;
import com.zluolan.zojbackendcommon.util.SimpleJwtTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证过滤器
 * 在网关层进行 JWT 令牌校验，并将用户信息传递给下游服务
 */
@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private SimpleJwtTool simpleJwtTool;

    /**
     * 不需要认证的路径
     */
    private static final List<String> SKIP_AUTH_PATHS = Arrays.asList(
            "/api/user/login",
            "/api/user/login/jwt",        // JWT登录接口
            "/api/user/register",
            "/api/user/login/wechat",
            "/api/question/list/page/vo",  // 题目列表
            "/api/question/get/vo",        // 题目详情
            "/api/question/get",           // 题目详情（备用）
            "/doc.html",
            "/v3/api-docs",
            "/swagger-ui",
            "/webjars"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        log.debug("JWT Auth Filter processing path: {}", path);
        
        // 检查是否需要跳过认证
        if (shouldSkipAuth(path)) {
            log.debug("Skipping auth for path: {}", path);
            return chain.filter(exchange);
        }
        
        // 获取 Authorization 头
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return handleUnauthorized(exchange);
        }
        
        String token = authHeader.substring(7); // 移除 "Bearer " 前缀
        
        try {
            // 解析 JWT 令牌（这里使用简化的实现）
            UserInfo userInfo = parseJwtToken(token);
            
            if (userInfo == null) {
                log.warn("Invalid JWT token for path: {}", path);
                return handleUnauthorized(exchange);
            }
            
            // 将用户信息添加到请求头中，传递给下游服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userInfo.getUserId()))
                    .header("X-User-Role", userInfo.getUserRole())
                    .header("X-User-Account", userInfo.getUserAccount())
                    .build();
            
            log.debug("JWT auth successful for user: {} on path: {}", userInfo.getUserAccount(), path);
            
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
            
        } catch (Exception e) {
            log.error("JWT token parsing error for path: {}", path, e);
            return handleUnauthorized(exchange);
        }
    }
    
    /**
     * 检查是否应该跳过认证
     */
    private boolean shouldSkipAuth(String path) {
        return SKIP_AUTH_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 解析 JWT 令牌
     * 支持标准JWT格式和简化格式（userId:userRole:userAccount）
     */
    private UserInfo parseJwtToken(String token) {
        try {
            // 首先尝试解析标准JWT格式
            if (token.contains(".")) {
                // 标准JWT格式，使用SimpleJwtTool解析
                if (!simpleJwtTool.validateToken(token)) {
                    return null;
                }
                
                UserInfo userInfo = new UserInfo();
                userInfo.setUserId(simpleJwtTool.getUserIdFromToken(token));
                userInfo.setUserRole(simpleJwtTool.getUserRoleFromToken(token));
                userInfo.setUserAccount(simpleJwtTool.getUserAccountFromToken(token));
                return userInfo;
            } else if (token.contains(":")) {
                // 简化格式（userId:userRole:userAccount）
                String[] parts = token.split(":");
                if (parts.length >= 3) {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setUserId(Long.parseLong(parts[0]));
                    userInfo.setUserRole(parts[1]);
                    userInfo.setUserAccount(parts[2]);
                    return userInfo;
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error parsing JWT token", e);
            return null;
        }
    }
    
    /**
     * 处理未授权请求
     */
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        String body = "{\"code\":401,\"message\":\"未登录\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -100; // 设置较高的优先级，确保在其他过滤器之前执行
    }
    
    /**
     * 用户信息类
     */
    private static class UserInfo {
        private Long userId;
        private String userRole;
        private String userAccount;
        
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getUserRole() {
            return userRole;
        }
        
        public void setUserRole(String userRole) {
            this.userRole = userRole;
        }
        
        public String getUserAccount() {
            return userAccount;
        }
        
        public void setUserAccount(String userAccount) {
            this.userAccount = userAccount;
        }
    }
}
