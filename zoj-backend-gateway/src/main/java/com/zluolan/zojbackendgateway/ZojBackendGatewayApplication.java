package com.zluolan.zojbackendgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.zluolan.zojbackendgateway", "com.zluolan.zojbackendcommon"})
public class ZojBackendGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZojBackendGatewayApplication.class, args);
    }

}