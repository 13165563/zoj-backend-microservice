package com.zluolan.zojbackendquestionservice.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI questionServiceOpenAPI() {
        return new OpenAPI().info(new Info().title("Question Service API").version("v1"));
    }

    @Bean
    public GroupedOpenApi questionApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .pathsToMatch("/**")
                .build();
    }
}


