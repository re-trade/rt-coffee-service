package org.retrade.feedback_notification.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.feedback_notification.model.dto.NotificationResponse;
import org.retrade.feedback_notification.service.WebSocketService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sentToUser(String userId, NotificationResponse notificationResponse) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notificationResponse);
    }

    @Override
    public void sentToAll(NotificationResponse notificationResponse) {
        messagingTemplate.convertAndSend("/topic/notifications", notificationResponse) ;
    }
}
