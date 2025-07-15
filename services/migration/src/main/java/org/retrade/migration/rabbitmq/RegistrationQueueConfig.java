package org.retrade.migration.rabbitmq;

import org.retrade.migration.constant.ExchangeNameEnum;
import org.retrade.migration.constant.QueueNameEnum;
import org.retrade.migration.constant.RoutingKeyEnum;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegistrationQueueConfig {
    @Bean
    public Declarables registrationQueueDeclarables() {
        DirectExchange registrationExchange = new DirectExchange(ExchangeNameEnum.REGISTRATION_EXCHANGE.getName());
        DirectExchange registrationRetryExchange = new DirectExchange(ExchangeNameEnum.REGISTRATION_RETRY_EXCHANGE.getName());

        Queue registrationQueue = QueueBuilder.durable(QueueNameEnum.USER_REGISTRATION_QUEUE.getName())
                .withArgument("x-dead-letter-exchange", registrationRetryExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.USER_REGISTRATION_RETRY_ROUTING_KEY.getName())
                .build();

        Queue registrationRetryQueue = QueueBuilder.durable(QueueNameEnum.USER_REGISTRATION_RETRY_QUEUE.getName())
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", registrationExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.USER_REGISTRATION_ROUTING_KEY.getName())
                .build();

        return new Declarables(
                registrationExchange,
                registrationRetryExchange,
                registrationQueue,
                registrationRetryQueue,
                BindingBuilder.bind(registrationQueue).to(registrationExchange)
                        .with(RoutingKeyEnum.USER_REGISTRATION_ROUTING_KEY.getName()),
                BindingBuilder.bind(registrationRetryQueue).to(registrationRetryExchange)
                        .with(RoutingKeyEnum.USER_REGISTRATION_RETRY_ROUTING_KEY.getName())
        );
    }
}
