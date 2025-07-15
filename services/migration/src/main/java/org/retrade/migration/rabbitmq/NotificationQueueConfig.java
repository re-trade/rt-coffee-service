package org.retrade.migration.rabbitmq;

import org.retrade.migration.constant.ExchangeNameEnum;
import org.retrade.migration.constant.QueueNameEnum;
import org.retrade.migration.constant.RoutingKeyEnum;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationQueueConfig {
    @Bean
    public Declarables notificationQueueDeclarables() {
        DirectExchange notificationExchange = new DirectExchange(ExchangeNameEnum.NOTIFICATION_EXCHANGE.getName());
        DirectExchange notificationRetryExchange = new DirectExchange(ExchangeNameEnum.NOTIFICATION_RETRY_EXCHANGE.getName());

        Queue emailNotificationQueue = QueueBuilder.durable(QueueNameEnum.EMAIL_NOTIFICATION_QUEUE.getName())
                .withArgument("x-dead-letter-exchange", notificationRetryExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.EMAIL_RETRY_ROUTING_KEY.getName())
                .build();

        Queue emailRetryQueue = QueueBuilder.durable(QueueNameEnum.EMAIL_RETRY_QUEUE.getName())
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", notificationExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.EMAIL_NOTIFICATION_ROUTING_KEY.getName())
                .build();

        Queue deadLetterQueue = QueueBuilder.durable(QueueNameEnum.DEAD_LETTER_QUEUE.getName()).build();

        return new Declarables(
                notificationExchange,
                notificationRetryExchange,
                emailNotificationQueue,
                emailRetryQueue,
                deadLetterQueue,
                BindingBuilder.bind(emailNotificationQueue).to(notificationExchange)
                        .with(RoutingKeyEnum.EMAIL_NOTIFICATION_ROUTING_KEY.getName()),
                BindingBuilder.bind(emailRetryQueue).to(notificationRetryExchange)
                        .with(RoutingKeyEnum.EMAIL_RETRY_ROUTING_KEY.getName()),
                BindingBuilder.bind(deadLetterQueue).to(notificationRetryExchange)
                        .with(RoutingKeyEnum.DEAD_LETTER_ROUTING_KEY.getName())
        );
    }
}
