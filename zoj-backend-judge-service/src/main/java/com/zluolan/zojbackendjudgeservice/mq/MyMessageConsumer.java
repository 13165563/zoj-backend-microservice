package com.zluolan.zojbackendjudgeservice.mq;

import com.rabbitmq.client.Channel;
import com.zluolan.zojbackendjudgeservice.config.RabbitMQConfig;
import com.zluolan.zojbackendjudgeservice.service.JudgeService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
@Slf4j
public class MyMessageConsumer {

    @Resource
    private JudgeService judgeService;

    // 指定程序监听的消息队列和确认机制
    @SneakyThrows
    @RabbitListener(queues = {RabbitMQConfig.CODE_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        try {
            // 处理单个ID或多个ID（逗号分隔）
            if (message.contains(",")) {
                // 多个ID，分别处理
                String[] ids = message.split(",");
                for (String id : ids) {
                    if (!id.trim().isEmpty()) {
                        long questionSubmitId = Long.parseLong(id.trim());
                        judgeService.doJudge(questionSubmitId);
                    }
                }
            } else {
                // 单个ID
                long questionSubmitId = Long.parseLong(message);
                judgeService.doJudge(questionSubmitId);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理消息失败: {}", message, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}