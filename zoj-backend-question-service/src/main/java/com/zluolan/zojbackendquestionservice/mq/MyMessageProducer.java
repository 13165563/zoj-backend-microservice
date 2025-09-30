package com.zluolan.zojbackendquestionservice.mq;

import com.zluolan.zojbackendquestionservice.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class MyMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.CODE_EXCHANGE_NAME, 
                RabbitMQConfig.CODE_ROUTING_KEY, message);
    }
}