package org.retrade.main.service;

import org.springframework.amqp.core.Message;

public interface DeadLetterQueueService {
    void handleDeadLetterMessage(Message message);
}
