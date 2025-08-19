package org.retrade.feedback_notification.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.feedback_notification.model.dto.NotificationRequest;
import org.retrade.feedback_notification.model.dto.NotificationResponse;
import org.retrade.feedback_notification.model.message.SocketNotificationMessage;

import java.util.List;

public interface NotificationService {
    PaginationWrapper<List<NotificationResponse>> getNotifications(QueryWrapper queryWrapper);

    void makeUserNotificationRead(SocketNotificationMessage message);

    NotificationResponse makeNotification(NotificationRequest notificationRequest);

    void testGlobalNotification();

    void markAsRead(String id);
}
