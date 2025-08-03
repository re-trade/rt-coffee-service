package org.retrade.main.config.rabbitmq;

import org.retrade.main.model.constant.ExchangeNameEnum;
import org.retrade.main.model.constant.QueueNameEnum;
import org.retrade.main.model.constant.RoutingKeyEnum;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AchievementQueueConfig {
    @Bean
    public Declarables achievementQueueDeclarables() {

        DirectExchange achievementExchange = new DirectExchange(ExchangeNameEnum.ACHIEVEMENT_EXCHANGE.getName());
        DirectExchange achievementRetryExchange = new DirectExchange(ExchangeNameEnum.ACHIEVEMENT_RETRY_EXCHANGE.getName());

        Queue sellerEventQueue = QueueBuilder.durable(QueueNameEnum.ACHIEVEMENT_SELLER_EVENT_QUEUE.getName())
                .withArgument("x-dead-letter-exchange", achievementRetryExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.ACHIEVEMENT_RETRY_ROUTING_KEY.getName())
                .build();

        Queue sellerEventRetryQueue = QueueBuilder.durable(QueueNameEnum.ACHIEVEMENT_SELLER_EVENT_RETRY_QUEUE.getName())
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", achievementExchange.getName())
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.ACHIEVEMENT_ROUTING_KEY.getName())
                .build();

        return new Declarables(
                achievementExchange,
                achievementRetryExchange,
                sellerEventQueue,
                sellerEventRetryQueue,
                BindingBuilder.bind(sellerEventQueue).to(achievementExchange)
                        .with(RoutingKeyEnum.ACHIEVEMENT_ROUTING_KEY.getName()),
                BindingBuilder.bind(sellerEventRetryQueue).to(achievementRetryExchange)
                        .with(RoutingKeyEnum.ACHIEVEMENT_RETRY_ROUTING_KEY.getName())
        );
    }
}
