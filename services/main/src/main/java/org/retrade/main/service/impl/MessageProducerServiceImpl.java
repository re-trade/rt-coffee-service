package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.message.MessageObject;
import org.retrade.main.config.RabbitMQConfig;
import org.retrade.main.model.message.CCCDVerificationMessage;
import org.retrade.main.model.message.EmailNotificationMessage;
import org.retrade.main.model.message.UserRegistrationMessage;
import org.retrade.main.service.MessageProducerService;
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
                RabbitMQConfig.ExchangeNameEnum.NOTIFICATION_EXCHANGE.getName(),
                RabbitMQConfig.RoutingKeyEnum.EMAIL_NOTIFICATION_ROUTING_KEY.getName(),
                messageWrapper
        );
        log.info("Email notification message sent: {}", message.getMessageId());
    }

    @Override
    public void sendUserRegistration(UserRegistrationMessage message) {
        if (message.getMessageId() == null) {
            message.setMessageId(UUID.randomUUID().toString());
        }
        var messageWrapper = new MessageObject.Builder<UserRegistrationMessage>()
                .withPayload(message)
                .withMessageId(message.getMessageId())
                .withSource("main-service")
                .withType("registration")
                .withTimestamp(LocalDateTime.now())
                .build();
        log.info("Sending user registration message: {}", message.getMessageId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ExchangeNameEnum.REGISTRATION_EXCHANGE.getName(),
                RabbitMQConfig.RoutingKeyEnum.USER_REGISTRATION_ROUTING_KEY.getName(),
                messageWrapper
        );
        log.info("User registration message sent: {}", message.getMessageId());
    }

    @Override
    public void sendCCCDForVerified(CCCDVerificationMessage message) {
        if (message.getMessageId() == null) {
            message.setMessageId(UUID.randomUUID().toString());
        }
        var messageWrapper = new MessageObject.Builder<CCCDVerificationMessage>()
                .withPayload(message)
                .withMessageId(message.getMessageId())
                .withSource("main-service")
                .withType("cccd-verified")
                .withTimestamp(LocalDateTime.now())
                .build();
        log.info("Sending seller verified message: {}", message.getMessageId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ExchangeNameEnum.IDENTITY_EXCHANGE.getName(),
                RabbitMQConfig.RoutingKeyEnum.IDENTITY_VERIFICATION_ROUTING_KEY.getName(),
                messageWrapper
        );
        log.info("Seller verified message sent: {}", message.getMessageId());
    }
}
