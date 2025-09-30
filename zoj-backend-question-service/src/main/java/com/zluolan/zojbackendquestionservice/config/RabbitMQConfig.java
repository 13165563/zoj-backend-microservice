package com.zluolan.zojbackendquestionservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String CODE_EXCHANGE_NAME = "code_exchange";
    public static final String CODE_QUEUE_NAME = "code_queue";
    public static final String CODE_ROUTING_KEY = "code_routingKey";
    
    /**
     * 创建交换机
     * @return
     */
    @Bean
    public Exchange codeExchange() {
        return ExchangeBuilder.directExchange(CODE_EXCHANGE_NAME).durable(true).build();
    }
    
    /**
     * 创建队列
     * @return
     */
    @Bean
    public Queue codeQueue() {
        return QueueBuilder.durable(CODE_QUEUE_NAME).build();
    }
    
    /**
     * 绑定交换机和队列
     * @return
     */
    @Bean
    public Binding binding(Queue codeQueue, Exchange codeExchange) {
        return BindingBuilder.bind(codeQueue).to(codeExchange).with(CODE_ROUTING_KEY).noargs();
    }
}