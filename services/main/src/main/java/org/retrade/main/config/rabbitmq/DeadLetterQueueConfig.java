package org.retrade.main.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeadLetterQueueConfig {
    @Bean
    public Declarables deadLetterQueueDeclarables() {
        Queue deadLetterQueue = QueueBuilder.durable("dead.letter.queue").build();

        DirectExchange deadLetterExchange = new DirectExchange("notification.dlx.exchange");
        return new Declarables(
                deadLetterQueue,
                BindingBuilder.bind(deadLetterQueue)
                        .to(deadLetterExchange).with("dead.letter")
        );
    }
}
