package org.retrade.main.config.rabbitmq;

import org.retrade.main.model.constant.ExchangeNameEnum;
import org.retrade.main.model.constant.QueueNameEnum;
import org.retrade.main.model.constant.RoutingKeyEnum;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityQueueConfig {
    @Bean
    public Declarables identityQueueDeclarables() {
        DirectExchange identityExchange = new DirectExchange(ExchangeNameEnum.IDENTITY_EXCHANGE.getName());
        DirectExchange identityRetryExchange = new DirectExchange(ExchangeNameEnum.IDENTITY_RETRY_EXCHANGE.getName());

        Queue identityVerificationQueue = QueueBuilder.durable(QueueNameEnum.IDENTITY_VERIFICATION_QUEUE.getName())
                .withArgument("x-dead-letter-exchange", identityRetryExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.IDENTITY_RETRY_ROUTING_KEY.getName())
                .build();

        Queue identityRetryQueue = QueueBuilder.durable(QueueNameEnum.IDENTITY_RETRY_QUEUE.getName())
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", identityExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.IDENTITY_VERIFICATION_ROUTING_KEY.getName())
                .build();

        Queue identityVerifiedResultQueue = QueueBuilder.durable(QueueNameEnum.IDENTITY_VERIFIED_RESULT_QUEUE.getName())
                .withArgument("x-dead-letter-exchange", identityRetryExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.IDENTITY_RETRY_ROUTING_KEY.getName())
                .build();

        Queue identityVerifiedRetryQueue = QueueBuilder.durable(QueueNameEnum.IDENTITY_VERIFIED_RETRY_QUEUE.getName())
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", identityExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.IDENTITY_VERIFIED_ROUTING_KEY.getName())
                .build();

        return new Declarables(
                identityExchange,
                identityRetryExchange,
                identityVerificationQueue,
                identityRetryQueue,
                identityVerifiedResultQueue,
                identityVerifiedRetryQueue,
                BindingBuilder.bind(identityVerificationQueue).to(identityExchange)
                        .with(RoutingKeyEnum.IDENTITY_VERIFICATION_ROUTING_KEY.getName()),
                BindingBuilder.bind(identityRetryQueue).to(identityRetryExchange)
                        .with(RoutingKeyEnum.IDENTITY_RETRY_ROUTING_KEY.getName()),
                BindingBuilder.bind(identityVerifiedResultQueue).to(identityExchange)
                        .with(RoutingKeyEnum.IDENTITY_VERIFIED_ROUTING_KEY.getName()),
                BindingBuilder.bind(identityVerifiedRetryQueue).to(identityRetryExchange)
                        .with(RoutingKeyEnum.IDENTITY_RETRY_ROUTING_KEY.getName())
        );
    }
}
