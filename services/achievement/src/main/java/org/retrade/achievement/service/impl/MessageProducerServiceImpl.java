package org.retrade.achievement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.achievement.model.constant.ExchangeNameEnum;
import org.retrade.achievement.model.constant.RoutingKeyEnum;
import org.retrade.achievement.model.message.EmailNotificationMessage;
import org.retrade.achievement.service.MessageProducerService;
import org.retrade.common.model.message.MessageObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducerServiceImpl implements MessageProducerService {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendEmailNotification(EmailNotificationMessage message) {
        if (message.getMessageId() == null) {
            message.setMessageId(UUID.randomUUID().toString());
        }
        var messageWrapper = new MessageObject.Builder<EmailNotificationMessage>()
                .withPayload(message)
                .withMessageId(message.getMessageId())
                .withSource("main-service")
                .withType("email")
                .withTimestamp(LocalDateTime.now())
                .build();
        log.info("Sending email notification message: {}", message.getMessageId());
        rabbitTemplate.convertAndSend(
                ExchangeNameEnum.NOTIFICATION_EXCHANGE.getName(),
                RoutingKeyEnum.EMAIL_NOTIFICATION_ROUTING_KEY.getName(),
                messageWrapper
        );
        log.info("Email notification message sent: {}", message.getMessageId());
    }

    @Override
    public void sendMessageToDeadQueue(Message rawMessage) {
        log.info("Sending message to dead queue: {}", rawMessage.getMessageProperties().getConsumerQueue());
        rabbitTemplate.send(ExchangeNameEnum.NOTIFICATION_EXCHANGE.getName(), RoutingKeyEnum.DEAD_LETTER_ROUTING_KEY.getName(), rawMessage);
    }
}
