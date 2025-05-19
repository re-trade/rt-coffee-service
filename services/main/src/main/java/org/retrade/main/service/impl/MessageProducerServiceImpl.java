package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.main.config.RabbitMQConfig;
import org.retrade.main.model.message.EmailNotificationMessage;
import org.retrade.main.model.message.UserRegistrationMessage;
import org.retrade.main.service.MessageProducerService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

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
        
        log.info("Sending email notification message: {}", message.getMessageId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ExchangeNameEnum.NOTIFICATION_EXCHANGE.getName(),
                RabbitMQConfig.QueueNameEnum.EMAIL_NOTIFICATION_QUEUE.getName(),
                message
        );
        log.info("Email notification message sent: {}", message.getMessageId());
    }

    @Override
    public void sendUserRegistration(UserRegistrationMessage message) {
        if (message.getMessageId() == null) {
            message.setMessageId(UUID.randomUUID().toString());
        }
        
        log.info("Sending user registration message: {}", message.getMessageId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ExchangeNameEnum.REGISTRATION_EXCHANGE.getName(),
                RabbitMQConfig.QueueNameEnum.USER_REGISTRATION_QUEUE.getName(),
                message
        );
        log.info("User registration message sent: {}", message.getMessageId());
    }
}
