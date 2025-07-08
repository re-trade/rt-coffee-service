package org.retrade.main.config.rabbitmq;

import org.retrade.main.model.constant.ExchangeNameEnum;
import org.retrade.main.model.constant.QueueNameEnum;
import org.retrade.main.model.constant.RoutingKeyEnum;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationQueueConfig {
    @Bean
    public Declarables notificationQueueDeclarables() {
        DirectExchange notificationExchange = new DirectExchange(ExchangeNameEnum.NOTIFICATION_EXCHANGE.name());
        DirectExchange notificationRetryExchange = new DirectExchange(ExchangeNameEnum.NOTIFICATION_RETRY_EXCHANGE.name());

        Queue emailNotificationQueue = QueueBuilder.durable(QueueNameEnum.EMAIL_NOTIFICATION_QUEUE.name())
                .withArgument("x-dead-letter-exchange", notificationRetryExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.EMAIL_RETRY_ROUTING_KEY.name())
                .build();

        Queue emailRetryQueue = QueueBuilder.durable(QueueNameEnum.EMAIL_RETRY_QUEUE.name())
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", notificationExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.EMAIL_NOTIFICATION_ROUTING_KEY.name())
                .build();

        Queue deadLetterQueue = QueueBuilder.durable(QueueNameEnum.DEAD_LETTER_QUEUE.name()).build();

        return new Declarables(
                notificationExchange,
                notificationRetryExchange,
                emailNotificationQueue,
                emailRetryQueue,
                deadLetterQueue,
                BindingBuilder.bind(emailNotificationQueue).to(notificationExchange)
                        .with(RoutingKeyEnum.EMAIL_NOTIFICATION_ROUTING_KEY.name()),
                BindingBuilder.bind(emailRetryQueue).to(notificationRetryExchange)
                        .with(RoutingKeyEnum.EMAIL_RETRY_ROUTING_KEY.name()),
                BindingBuilder.bind(deadLetterQueue).to(notificationRetryExchange)
                        .with(RoutingKeyEnum.DEAD_LETTER_ROUTING_KEY.name())
        );
    }
}
