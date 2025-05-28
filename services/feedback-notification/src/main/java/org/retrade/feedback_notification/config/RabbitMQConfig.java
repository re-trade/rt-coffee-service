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
    public static final String NOTIFICATION_RETRY_EXCHANGE = "notification.dlx.exchange";
    public static final String REGISTRATION_EXCHANGE = "registration.exchange";
    public static final String REGISTRATION_RETRY_EXCHANGE = "registration.dlx.exchange";

    public static final String EMAIL_NOTIFICATION_QUEUE = "email.notification.queue";
    public static final String EMAIL_RETRY_QUEUE = "email.dlx.queue";
    public static final String USER_REGISTRATION_QUEUE = "user.registration.queue";
    public static final String USER_REGISTRATION_RETRY_QUEUE = "user.registration.dlx.queue";
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";

    public static final String EMAIL_NOTIFICATION_ROUTING_KEY = "email.notification";
    public static final String EMAIL_RETRY_ROUTING_KEY = "email.retry";
    public static final String USER_REGISTRATION_ROUTING_KEY = "user.registration";
    public static final String USER_REGISTRATION_RETRY_ROUTING_KEY = "user.registration.retry";
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
        DirectExchange notificationExchange = new DirectExchange(NOTIFICATION_EXCHANGE);
        DirectExchange registrationExchange = new DirectExchange(REGISTRATION_EXCHANGE);
        DirectExchange notificationRetryExchange = new DirectExchange(NOTIFICATION_RETRY_EXCHANGE);
        DirectExchange registrationRetryExchange = new DirectExchange(REGISTRATION_RETRY_EXCHANGE);

        Queue emailNotificationQueue = QueueBuilder.durable(EMAIL_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_RETRY_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", EMAIL_RETRY_ROUTING_KEY)
                .build();

        Queue emailRetryQueue = QueueBuilder.durable(EMAIL_RETRY_QUEUE)
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", EMAIL_NOTIFICATION_ROUTING_KEY)
                .build();

        Queue userRegistrationQueue = QueueBuilder.durable(USER_REGISTRATION_QUEUE)
                .withArgument("x-dead-letter-exchange", REGISTRATION_RETRY_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", USER_REGISTRATION_RETRY_ROUTING_KEY)
                .build();

        Queue userRegistrationRetryQueue = QueueBuilder.durable(USER_REGISTRATION_RETRY_QUEUE)
                .withArgument("x-message-ttl", 30000)
                .withArgument("x-dead-letter-exchange", REGISTRATION_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", USER_REGISTRATION_ROUTING_KEY)
                .build();

        Queue deadLetterQueue = QueueBuilder.durable(DEAD_LETTER_QUEUE).build();

        Binding emailBinding = BindingBuilder.bind(emailNotificationQueue)
                .to(notificationExchange)
                .with(EMAIL_NOTIFICATION_ROUTING_KEY);

        Binding emailRetryBinding = BindingBuilder.bind(emailRetryQueue)
                .to(notificationRetryExchange)
                .with(EMAIL_RETRY_ROUTING_KEY);

        Binding registrationBinding = BindingBuilder.bind(userRegistrationQueue)
                .to(registrationExchange)
                .with(USER_REGISTRATION_ROUTING_KEY);

        Binding registrationRetryBinding = BindingBuilder.bind(userRegistrationRetryQueue)
                .to(registrationRetryExchange)
                .with(USER_REGISTRATION_RETRY_ROUTING_KEY);

        Binding deadLetterBinding = BindingBuilder.bind(deadLetterQueue)
                .to(notificationRetryExchange)
                .with(DEAD_LETTER_ROUTING_KEY);

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
                deadLetterBinding
        );
    }
}