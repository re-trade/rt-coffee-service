package org.retrade.achievement.service;

import org.retrade.achievement.model.message.EmailNotificationMessage;
import org.springframework.amqp.core.Message;

public interface MessageProducerService {

    void sendEmailNotification(EmailNotificationMessage message);

    void sendMessageToDeadQueue(Message rawMessage);
}
