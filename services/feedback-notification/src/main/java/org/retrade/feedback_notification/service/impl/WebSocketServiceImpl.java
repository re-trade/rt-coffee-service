package org.retrade.feedback_notification.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.feedback_notification.service.WebSocketService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(destination,
                "/queue/notifications",
                payload);
    }
}
