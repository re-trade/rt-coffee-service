package org.retrade.main.service;

import org.retrade.main.model.message.CCCDVerificationMessage;
import org.retrade.main.model.message.EmailNotificationMessage;
import org.retrade.main.model.message.UserRegistrationMessage;

public interface MessageProducerService {
    void sendEmailNotification(EmailNotificationMessage message);
    void sendUserRegistration(UserRegistrationMessage message);

    void sendCCCDForVerified(CCCDVerificationMessage message);
}
