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
        DirectExchange identityExchange = new DirectExchange(ExchangeNameEnum.IDENTITY_EXCHANGE.name());
        DirectExchange identityRetryExchange = new DirectExchange(ExchangeNameEnum.IDENTITY_RETRY_EXCHANGE.name());

        Queue identityVerificationQueue = QueueBuilder.durable(QueueNameEnum.IDENTITY_VERIFICATION_QUEUE.name())
                .withArgument("x-dead-letter-exchange", identityRetryExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.IDENTITY_RETRY_ROUTING_KEY.name())
                .build();

        Queue identityRetryQueue = QueueBuilder.durable(QueueNameEnum.IDENTITY_RETRY_QUEUE.name())
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", identityExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.IDENTITY_VERIFICATION_ROUTING_KEY.name())
                .build();

        Queue identityVerifiedResultQueue = QueueBuilder.durable(QueueNameEnum.IDENTITY_VERIFIED_RESULT_QUEUE.name())
                .withArgument("x-dead-letter-exchange", identityRetryExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.IDENTITY_RETRY_ROUTING_KEY.name())
                .build();

        Queue identityVerifiedRetryQueue = QueueBuilder.durable(QueueNameEnum.IDENTITY_VERIFIED_RETRY_QUEUE.name())
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", identityExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.IDENTITY_VERIFIED_ROUTING_KEY.name())
                .build();

        return new Declarables(
                identityExchange,
                identityRetryExchange,
                identityVerificationQueue,
                identityRetryQueue,
                identityVerifiedResultQueue,
                identityVerifiedRetryQueue,
                BindingBuilder.bind(identityVerificationQueue).to(identityExchange)
                        .with(RoutingKeyEnum.IDENTITY_VERIFICATION_ROUTING_KEY.name()),
                BindingBuilder.bind(identityRetryQueue).to(identityRetryExchange)
                        .with(RoutingKeyEnum.IDENTITY_RETRY_ROUTING_KEY.name()),
                BindingBuilder.bind(identityVerifiedResultQueue).to(identityExchange)
                        .with(RoutingKeyEnum.IDENTITY_VERIFIED_ROUTING_KEY.name()),
                BindingBuilder.bind(identityVerifiedRetryQueue).to(identityRetryExchange)
                        .with(RoutingKeyEnum.IDENTITY_RETRY_ROUTING_KEY.name())
        );
    }
}
