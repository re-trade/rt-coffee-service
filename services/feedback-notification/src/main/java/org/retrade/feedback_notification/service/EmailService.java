package org.retrade.feedback_notification.service;

import org.retrade.feedback_notification.model.message.EmailNotificationMessage;

public interface EmailService {
    void sendEmail(EmailNotificationMessage emailNotification);
}
