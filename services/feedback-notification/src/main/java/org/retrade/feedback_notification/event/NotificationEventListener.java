package org.retrade.feedback_notification.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.message.MessageObject;
import org.retrade.feedback_notification.config.common.RabbitMQConfig;
import org.retrade.feedback_notification.model.message.EmailNotificationMessage;
import org.retrade.feedback_notification.model.message.SocketNotificationMessage;
import org.retrade.feedback_notification.service.EmailService;
import org.retrade.feedback_notification.service.NotificationService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {
    private final EmailService emailService;
    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.USER_REGISTRATION_QUEUE)
    public void processUserRegistration(Message rawMessage, Channel channel) throws IOException {
        long deliveryTag = rawMessage.getMessageProperties().getDeliveryTag();


        SocketNotificationMessage message = null;
        try {
            byte[] body = rawMessage.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MessageObject<SocketNotificationMessage> wrapper = objectMapper.readValue(
                    body,
                    new TypeReference<>() {}
            );
            message = wrapper.getPayload();
            log.info("Processing socket notification message: {}", message.getMessageId());
            notificationService.makeUserNotificationRead(message);
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
