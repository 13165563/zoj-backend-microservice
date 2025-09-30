package com.zluolan.zojbackendquestionservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.zluolan.zojbackendserviceclient.service"})
@ComponentScan(basePackages = {"com.zluolan.zojbackendquestionservice", "com.zluolan.zojbackendcommon"})
@MapperScan("com.zluolan.zojbackendquestionservice.mapper")
public class ZojBackendQuestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZojBackendQuestionServiceApplication.class, args);
    }

}