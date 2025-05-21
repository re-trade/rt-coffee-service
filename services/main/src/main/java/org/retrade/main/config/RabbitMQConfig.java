package org.retrade.main.config;

import lombok.Getter;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMQConfig {
    @Getter
    public enum ExchangeNameEnum {
        NOTIFICATION_EXCHANGE("notification.exchange"),
        REGISTRATION_EXCHANGE("registration.exchange"),
        DEAD_LETTER_EXCHANGE("dead.letter.exchange");

        private final String name;
        ExchangeNameEnum(String name) {
            this.name = name;
        }
    }

    @Getter
    public enum QueueNameEnum {
        EMAIL_NOTIFICATION_QUEUE("email.notification.queue"),
        USER_REGISTRATION_QUEUE("user.registration.queue"),
        DEAD_LETTER_QUEUE("dead.letter.queue");
        private final String name;
        QueueNameEnum(String name) {
            this.name = name;
        }
    }

    @Getter
    public enum RoutingKeyEnum {
        EMAIL_NOTIFICATION_ROUTING_KEY("email.notification"),
        USER_REGISTRATION_ROUTING_KEY("user.registration"),
        DEAD_LETTER_ROUTING_KEY("dead.letter");
        private final String name;
        RoutingKeyEnum(String name) {
            this.name = name;
        }
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    @Bean
    public Declarables rabbitDeclarables() {
        DirectExchange notificationExchange = new DirectExchange(ExchangeNameEnum.NOTIFICATION_EXCHANGE.name);
        DirectExchange registrationExchange = new DirectExchange(ExchangeNameEnum.REGISTRATION_EXCHANGE.name);

        Queue emailNotificationQueue = QueueBuilder.durable(QueueNameEnum.EMAIL_NOTIFICATION_QUEUE.name)
                .build();

        Queue userRegistrationQueue = QueueBuilder.durable(QueueNameEnum.USER_REGISTRATION_QUEUE.name)
                .build();

        Binding emailBinding = BindingBuilder.bind(emailNotificationQueue)
                .to(notificationExchange)
                .with(RoutingKeyEnum.EMAIL_NOTIFICATION_ROUTING_KEY.name);

        Binding registrationBinding = BindingBuilder.bind(userRegistrationQueue)
                .to(registrationExchange)
                .with(RoutingKeyEnum.USER_REGISTRATION_ROUTING_KEY.name);

        return new Declarables(
                notificationExchange,
                registrationExchange,
                emailNotificationQueue,
                userRegistrationQueue,
                emailBinding,
                registrationBinding
        );
    }
}
