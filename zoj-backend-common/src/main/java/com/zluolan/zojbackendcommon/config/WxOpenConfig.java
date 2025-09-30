package com.zluolan.zojbackendcommon.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信开放平台配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx.open")
public class WxOpenConfig {

    private String appId;

    private String appSecret;

    private String redirectUrl;
}