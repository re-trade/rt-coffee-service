package org.retrade.main.service;

import org.retrade.main.model.message.*;
import org.springframework.amqp.core.Message;

public interface MessageProducerService {
    void sendEmailNotification(EmailNotificationMessage message);
    void sendUserRegistration(UserRegistrationMessage message);

    void sendCCCDForVerified(CCCDVerificationMessage message);

    void sendSocketNotification(SocketNotificationMessage message);

    void sendAchievementMessage(AchievementMessage message);

    void sendMessageToDeadQueue(Message rawMessage);
}
