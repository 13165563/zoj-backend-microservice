package com.zluolan.zojbackenduserservice.config;

import lombok.Data;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wx.mp")
@Data
public class WxMpConfig {

    private String appId;
    private String secret;
    private String token;
    private String aesKey;

    @Bean
    public WxMpService wxMpService() {
        WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
        config.setAppId(appId);
        config.setSecret(secret);
        config.setToken(token);
        config.setAesKey(aesKey);
        WxMpService service = new WxMpServiceImpl();
        service.setWxMpConfigStorage(config);
        return service;
    }
}


