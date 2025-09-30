package com.zluolan.zojbackenduserservice;

import com.zluolan.zojbackendcommon.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableFeignClients(basePackages = "com.zluolan.zojbackendserviceclient.service", defaultConfiguration = DefaultFeignConfig.class)
@ComponentScan("com.zluolan")
public class ZojBackendUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZojBackendUserServiceApplication.class, args);
    }

}