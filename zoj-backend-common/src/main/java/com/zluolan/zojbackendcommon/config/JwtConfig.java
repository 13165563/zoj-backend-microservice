package com.zluolan.zojbackendcommon.config;

import com.zluolan.zojbackendcommon.util.SimpleJwtTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置类
 */
@Slf4j
@Configuration
public class JwtConfig {

    @Bean
    public SimpleJwtTool simpleJwtTool() {
        log.info("创建JWT工具Bean");
        SimpleJwtTool jwtTool = new SimpleJwtTool();
        jwtTool.init();
        return jwtTool;
    }
}
