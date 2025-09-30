package com.zluolan.zojbackendcommon.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    // 暂时移除JWT相关配置，让服务能够启动
    
    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        // JWT拦截器暂时禁用
        log.info("JWT拦截器暂时禁用");
    }
}