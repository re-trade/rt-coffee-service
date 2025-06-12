package org.retrade.prover.config;

import lombok.Getter;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
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
        NOTIFICATION_RETRY_EXCHANGE("notification.dlx.exchange"),
        REGISTRATION_EXCHANGE("registration.exchange"),
        REGISTRATION_RETRY_EXCHANGE("registration.dlx.exchange"),        IDENTITY_EXCHANGE("identity.exchange"),
        IDENTITY_RETRY_EXCHANGE("identity.dlx.exchange");
        private final String name;
        ExchangeNameEnum(String name) {
            this.name = name;
        }
    }

    @Getter
    public enum QueueNameEnum {
        EMAIL_NOTIFICATION_QUEUE("email.notification.queue"),
        EMAIL_RETRY_QUEUE("email.dlx.queue"),
        USER_REGISTRATION_QUEUE("user.registration.queue"),
        USER_REGISTRATION_RETRY_QUEUE("user.registration.dlx.queue"),
        DEAD_LETTER_QUEUE("dead.letter.queue"),
        IDENTITY_VERIFICATION_QUEUE("identity.verification.queue"),
        IDENTITY_RETRY_QUEUE("identity.dlx.queue");
        private final String name;
        QueueNameEnum(String name) {
            this.name = name;
        }
    }

    @Getter
    public enum RoutingKeyEnum {
        EMAIL_NOTIFICATION_ROUTING_KEY("email.notification"),
        EMAIL_RETRY_ROUTING_KEY("email.retry"),
        USER_REGISTRATION_ROUTING_KEY("user.registration"),
        USER_REGISTRATION_RETRY_ROUTING_KEY("user.registration.retry"),
        DEAD_LETTER_ROUTING_KEY("dead.letter"),
        IDENTITY_VERIFICATION_ROUTING_KEY("identity.verification"),
        IDENTITY_RETRY_ROUTING_KEY("identity.retry");
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
                .maxAttempts(2)
                .backOffOptions(1000, 1.5, 5000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, RetryOperationsInterceptor retryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setAdviceChain(retryInterceptor);
        return factory;
    }

    @Bean
    public Declarables rabbitDeclarables() {
        DirectExchange notificationExchange = new DirectExchange(ExchangeNameEnum.NOTIFICATION_EXCHANGE.name);
        DirectExchange registrationExchange = new DirectExchange(ExchangeNameEnum.REGISTRATION_EXCHANGE.name);
        DirectExchange notificationRetryExchange = new DirectExchange(ExchangeNameEnum.NOTIFICATION_RETRY_EXCHANGE.name);
        DirectExchange registrationRetryExchange = new DirectExchange(ExchangeNameEnum.REGISTRATION_RETRY_EXCHANGE.name);
        DirectExchange identityExchange = new DirectExchange(ExchangeNameEnum.IDENTITY_EXCHANGE.name);
        DirectExchange identityRetryExchange = new DirectExchange(ExchangeNameEnum.IDENTITY_RETRY_EXCHANGE.name);


        Queue emailNotificationQueue = QueueBuilder.durable(QueueNameEnum.EMAIL_NOTIFICATION_QUEUE.name)
                .withArgument("x-dead-letter-exchange", ExchangeNameEnum.NOTIFICATION_RETRY_EXCHANGE.name)
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.EMAIL_RETRY_ROUTING_KEY.name)
                .build();

        Queue emailRetryQueue = QueueBuilder.durable(QueueNameEnum.EMAIL_RETRY_QUEUE.name)
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", ExchangeNameEnum.NOTIFICATION_EXCHANGE.name)
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.EMAIL_NOTIFICATION_ROUTING_KEY.name)
                .build();

        Queue userRegistrationQueue = QueueBuilder.durable(QueueNameEnum.USER_REGISTRATION_QUEUE.name)
                .withArgument("x-dead-letter-exchange", ExchangeNameEnum.REGISTRATION_RETRY_EXCHANGE.name)
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.USER_REGISTRATION_RETRY_ROUTING_KEY.name)
                .build();

        Queue userRegistrationRetryQueue = QueueBuilder.durable(QueueNameEnum.USER_REGISTRATION_RETRY_QUEUE.name)
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", ExchangeNameEnum.REGISTRATION_EXCHANGE.name)
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.USER_REGISTRATION_ROUTING_KEY.name)
                .build();

        Queue identityVerificationQueue = QueueBuilder.durable(QueueNameEnum.IDENTITY_VERIFICATION_QUEUE.name)
                .withArgument("x-dead-letter-exchange", ExchangeNameEnum.IDENTITY_RETRY_EXCHANGE.name)
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.IDENTITY_RETRY_ROUTING_KEY.name)
                .build();

        Queue identityRetryQueue = QueueBuilder.durable(QueueNameEnum.IDENTITY_RETRY_QUEUE.name)
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", ExchangeNameEnum.IDENTITY_EXCHANGE.name)
                .withArgument("x-dead-letter-routing-key", RoutingKeyEnum.IDENTITY_VERIFICATION_ROUTING_KEY.name)
                .build();


        Queue deadLetterQueue = QueueBuilder.durable(QueueNameEnum.DEAD_LETTER_QUEUE.name).build();

        Binding emailBinding = BindingBuilder.bind(emailNotificationQueue)
                .to(notificationExchange)
                .with(RoutingKeyEnum.EMAIL_NOTIFICATION_ROUTING_KEY.name);

        Binding emailRetryBinding = BindingBuilder.bind(emailRetryQueue)
                .to(notificationRetryExchange)
                .with(RoutingKeyEnum.EMAIL_RETRY_ROUTING_KEY.name);

        Binding registrationBinding = BindingBuilder.bind(userRegistrationQueue)
                .to(registrationExchange)
                .with(RoutingKeyEnum.USER_REGISTRATION_ROUTING_KEY.name);

        Binding registrationRetryBinding = BindingBuilder.bind(userRegistrationRetryQueue)
                .to(registrationRetryExchange)
                .with(RoutingKeyEnum.USER_REGISTRATION_RETRY_ROUTING_KEY.name);

        Binding deadLetterBinding = BindingBuilder.bind(deadLetterQueue)
                .to(notificationRetryExchange)
                .with(RoutingKeyEnum.DEAD_LETTER_ROUTING_KEY.name);

        Binding identityBinding = BindingBuilder.bind(identityVerificationQueue)
                .to(identityExchange)
                .with(RoutingKeyEnum.IDENTITY_VERIFICATION_ROUTING_KEY.name);

        Binding identityRetryBinding = BindingBuilder.bind(identityRetryQueue)
                .to(identityRetryExchange)
                .with(RoutingKeyEnum.IDENTITY_RETRY_ROUTING_KEY.name);

        return new Declarables(
                notificationExchange,
                registrationExchange,
                notificationRetryExchange,
                registrationRetryExchange,
                emailNotificationQueue,
                emailRetryQueue,
                userRegistrationQueue,
                userRegistrationRetryQueue,
                deadLetterQueue,
                emailBinding,
                emailRetryBinding,
                registrationBinding,
                registrationRetryBinding,
                deadLetterBinding,
                identityExchange,
                identityRetryExchange,
                identityVerificationQueue,
                identityRetryQueue,
                identityBinding,
                identityRetryBinding
        );
    }
}
