package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.main.service.RabbitMQHealthService;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQHealthServiceImpl implements RabbitMQHealthService {

    private final ConnectionFactory connectionFactory;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public boolean isRabbitMQHealthy() {
        try {
            // Check if the connection is open
            if (!connectionFactory.createConnection().isOpen()) {
                log.error("RabbitMQ connection is not open");
                return false;
            }
            
            // Check if we can get a channel
            rabbitTemplate.execute(channel -> {
                if (!channel.isOpen()) {
                    throw new RuntimeException("Channel is not open");
                }
                return null;
            });
            
            return true;
        } catch (Exception e) {
            log.error("RabbitMQ health check failed", e);
            return false;
        }
    }
}
