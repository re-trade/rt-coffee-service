package org.retrade.main.service;

import org.retrade.main.model.message.CCCDVerificationMessage;
import org.retrade.main.model.message.EmailNotificationMessage;
import org.retrade.main.model.message.SocketNotificationMessage;
import org.retrade.main.model.message.UserRegistrationMessage;
import org.springframework.amqp.core.Message;

public interface MessageProducerService {
    void sendEmailNotification(EmailNotificationMessage message);
    void sendUserRegistration(UserRegistrationMessage message);

    void sendCCCDForVerified(CCCDVerificationMessage message);

    void sendSocketNotification(SocketNotificationMessage message);

    void sendMessageToDeadQueue(Message rawMessage);
}
