package org.retrade.main.service.impl;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.main.config.RabbitMQConfig;
import org.retrade.main.model.message.EmailNotificationMessage;
import org.retrade.main.model.message.UserRegistrationMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageConsumerServiceImpl {

    private final RabbitTemplate rabbitTemplate;
    private static final int MAX_RETRIES = 3;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_NOTIFICATION_QUEUE)
    public void processEmailNotification(EmailNotificationMessage message, Message amqpMessage, Channel channel) throws IOException {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            log.info("Processing email notification message: {}", message.getMessageId());
            
            // TODO: Implement actual email sending logic here
            
            // Simulate processing
            log.info("Sending email to: {}, subject: {}", message.getTo(), message.getSubject());
            
            // Acknowledge the message
            channel.basicAck(deliveryTag, false);
            log.info("Email notification message processed successfully: {}", message.getMessageId());
        } catch (Exception e) {
            handleMessageProcessingFailure(message, amqpMessage, channel, deliveryTag, e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.USER_REGISTRATION_QUEUE)
    public void processUserRegistration(UserRegistrationMessage message, Message amqpMessage, Channel channel) throws IOException {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            log.info("Processing user registration message: {}", message.getMessageId());
            
            // TODO: Implement actual user registration processing logic here
            
            // Simulate processing
            log.info("Processing registration for user: {}, email: {}", message.getUsername(), message.getEmail());
            
            // Acknowledge the message
            channel.basicAck(deliveryTag, false);
            log.info("User registration message processed successfully: {}", message.getMessageId());
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
            // Reject the message and requeue it
            log.info("Retrying message, attempt: {}", retryCount + 1);
            channel.basicNack(deliveryTag, false, true);
        } else {
            // Reject the message and don't requeue it (it will go to the dead letter queue)
            log.warn("Max retries reached for message. Sending to dead letter queue.");
            channel.basicNack(deliveryTag, false, false);
            
            // You could also implement additional error handling here, such as:
            // - Sending an alert
            // - Logging to a monitoring system
            // - Storing the failed message in a database for later analysis
        }
    }

    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE)
    public void processDeadLetterQueue(Message message) {
        log.error("Message in dead letter queue: {}", message);
        // Implement dead letter queue handling logic
        // This could include:
        // - Logging the failed message
        // - Sending alerts
        // - Storing in a database for manual review
    }
}
