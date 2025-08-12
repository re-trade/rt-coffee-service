package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.message.MessageObject;
import org.retrade.main.model.constant.ExchangeNameEnum;
import org.retrade.main.model.constant.RoutingKeyEnum;
import org.retrade.main.model.message.CCCDVerificationMessage;
import org.retrade.main.model.message.EmailNotificationMessage;
import org.retrade.main.model.message.SocketNotificationMessage;
import org.retrade.main.model.message.UserRegistrationMessage;
import org.retrade.main.service.MessageProducerService;
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
                ExchangeNameEnum.REGISTRATION_EXCHANGE.getName(),
                RoutingKeyEnum.USER_REGISTRATION_ROUTING_KEY.getName(),
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
                ExchangeNameEnum.IDENTITY_EXCHANGE.getName(),
                RoutingKeyEnum.IDENTITY_VERIFICATION_ROUTING_KEY.getName(),
                messageWrapper
        );
        log.info("Seller verified message sent: {}", message.getMessageId());
    }

    @Override
    public void sendSocketNotification(SocketNotificationMessage message) {
        if (message.getMessageId() == null) {
            message.setMessageId(UUID.randomUUID().toString());
        }
        var messageWrapper = new MessageObject.Builder<SocketNotificationMessage>()
                .withPayload(message)
                .withMessageId(message.getMessageId())
                .withSource("main-service")
                .withType("socket-notification")
                .withTimestamp(LocalDateTime.now())
                .build();
        log.info("Sending socket notification message: {}", message.getMessageId());
        rabbitTemplate.convertAndSend(
                ExchangeNameEnum.NOTIFICATION_EXCHANGE.getName(),
                RoutingKeyEnum.SOCKET_NOTIFICATION_ROUTING_KEY.getName(),
                messageWrapper
        );
    }

    @Override
    public void sendMessageToDeadQueue(Message rawMessage) {
        log.info("Sending message to dead queue: {}", rawMessage.getMessageProperties().getConsumerQueue());
        rabbitTemplate.send(ExchangeNameEnum.NOTIFICATION_EXCHANGE.getName(), RoutingKeyEnum.DEAD_LETTER_ROUTING_KEY.getName(), rawMessage);
    }
}
