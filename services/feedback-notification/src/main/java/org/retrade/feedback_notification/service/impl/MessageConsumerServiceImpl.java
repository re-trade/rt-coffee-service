package org.retrade.feedback_notification.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.message.MessageObject;
import org.retrade.feedback_notification.config.RabbitMQConfig;
import org.retrade.feedback_notification.model.message.EmailNotificationMessage;
import org.retrade.feedback_notification.model.message.UserRegistrationMessage;
import org.retrade.feedback_notification.service.EmailService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageConsumerServiceImpl {
    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.USER_REGISTRATION_QUEUE)
    public void processUserRegistration(Message rawMessage, Channel channel) throws IOException {
        long deliveryTag = rawMessage.getMessageProperties().getDeliveryTag();


        UserRegistrationMessage message = null;
        try {
            byte[] body = rawMessage.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MessageObject<UserRegistrationMessage> wrapper = objectMapper.readValue(
                    body,
                    new TypeReference<>() {}
            );
            message = wrapper.getPayload();
            log.info("Processing user registration message: {}", message.getMessageId());
            log.info("User registered: {}, email: {}, userId: {}",
                    message.getUsername(),
                    message.getEmail(),
                    message.getUserId());
            channel.basicAck(deliveryTag, false);
            log.info("User registration message processed successfully: {}", message.getMessageId());
        } catch (Exception e) {
            log.error("Error processing user registration message: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_NOTIFICATION_QUEUE)
    public void processEmailNotification(Message rawMessage, Channel channel) throws IOException {
        long deliveryTag = rawMessage.getMessageProperties().getDeliveryTag();
        try {
            EmailNotificationMessage message;
            byte[] body = rawMessage.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MessageObject<EmailNotificationMessage> wrapper = objectMapper.readValue(
                    body,
                    new TypeReference<>() {}
            );
            message = wrapper.getPayload();
            log.info("Processing email notification message: {}", message.getMessageId());
            emailService.sendEmail(message);
            channel.basicAck(deliveryTag, false);
            log.info("Email notification message processed successfully: {}", message.getMessageId());
        } catch (Exception e) {
            log.error("Error processing email notification message: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE)
    public void processDeadLetterQueue(Message message) {
        log.error("Message in dead letter queue: {}", message);
        String messageId = message.getMessageProperties().getMessageId();
        String exchange = message.getMessageProperties().getReceivedExchange();
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String contentType = message.getMessageProperties().getContentType();

        log.error("Dead letter message details - ID: {}, Exchange: {}, Routing Key: {}, Content Type: {}",
                messageId, exchange, routingKey, contentType);
    }
}
