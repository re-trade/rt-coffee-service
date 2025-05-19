package org.retrade.feedback_notification.service.impl;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private static final int MAX_RETRIES = 3;

    @RabbitListener(queues = RabbitMQConfig.USER_REGISTRATION_QUEUE)
    public void processUserRegistration(UserRegistrationMessage message, Message amqpMessage, Channel channel) throws IOException {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            log.info("Processing user registration message: {}", message.getMessageId());

            log.info("User registered: {}, email: {}, userId: {}",
                    message.getUsername(),
                    message.getEmail(),
                    message.getUserId());

            channel.basicAck(deliveryTag, false);
            log.info("User registration message processed successfully: {}", message.getMessageId());
        } catch (Exception e) {
            handleMessageProcessingFailure(message, amqpMessage, channel, deliveryTag, e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_NOTIFICATION_QUEUE)
    public void processEmailNotification(EmailNotificationMessage message, Message amqpMessage, Channel channel) throws IOException {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            log.info("Processing email notification message: {}", message.getMessageId());
            emailService.sendEmail(message);
            channel.basicAck(deliveryTag, false);
            log.info("Email notification message processed successfully: {}", message.getMessageId());
        } catch (Exception e) {
            handleMessageProcessingFailure(message, amqpMessage, channel, deliveryTag, e);
        }
    }

    private void handleMessageProcessingFailure(Object message, Message amqpMessage, Channel channel, long deliveryTag, Exception e) throws IOException {
        log.error("Error processing message: {}", e.getMessage(), e);

        int retryCount = 0;
        if (message instanceof EmailNotificationMessage) {
            retryCount = ((EmailNotificationMessage) message).getRetryCount();
            ((EmailNotificationMessage) message).setRetryCount(retryCount + 1);
        } else if (message instanceof UserRegistrationMessage) {
            retryCount = ((UserRegistrationMessage) message).getRetryCount();
            ((UserRegistrationMessage) message).setRetryCount(retryCount + 1);
        }

        if (retryCount < MAX_RETRIES) {
            log.info("Retrying message, attempt: {}", retryCount + 1);
            channel.basicNack(deliveryTag, false, true);
        } else {
            log.warn("Max retries reached for message. Sending to dead letter queue.");
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
