package org.retrade.feedback_notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
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
@EnableRabbit
public class RabbitMQConfig {

    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String REGISTRATION_EXCHANGE = "registration.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "dead.letter.exchange";

    public static final String EMAIL_NOTIFICATION_QUEUE = "email.notification.queue";
    public static final String USER_REGISTRATION_QUEUE = "user.registration.queue";
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";

    public static final String EMAIL_NOTIFICATION_ROUTING_KEY = "email.notification";
    public static final String USER_REGISTRATION_ROUTING_KEY = "user.registration";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
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
        DirectExchange notificationExchange = new DirectExchange(NOTIFICATION_EXCHANGE);
        DirectExchange registrationExchange = new DirectExchange(REGISTRATION_EXCHANGE);

        Queue emailNotificationQueue = QueueBuilder.durable(EMAIL_NOTIFICATION_QUEUE)
                .build();

        Queue userRegistrationQueue = QueueBuilder.durable(USER_REGISTRATION_QUEUE)
                .build();

        Binding emailBinding = BindingBuilder.bind(emailNotificationQueue)
                .to(notificationExchange)
                .with(EMAIL_NOTIFICATION_ROUTING_KEY);

        Binding registrationBinding = BindingBuilder.bind(userRegistrationQueue)
                .to(registrationExchange)
                .with(USER_REGISTRATION_ROUTING_KEY);

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