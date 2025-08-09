package org.retrade.feedback_notification.service;

import org.retrade.feedback_notification.model.dto.NotificationResponse;

public interface WebSocketService {
    void sentToUser(String userId, NotificationResponse notificationResponse);

    void sentToAll(NotificationResponse notificationResponse);
}
