package org.retrade.main.config;

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

    // Exchange names
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String REGISTRATION_EXCHANGE = "registration.exchange";

    // Queue names
    public static final String EMAIL_NOTIFICATION_QUEUE = "email.notification.queue";
    public static final String USER_REGISTRATION_QUEUE = "user.registration.queue";
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";

    // Routing keys
    public static final String EMAIL_NOTIFICATION_ROUTING_KEY = "email.notification";
    public static final String USER_REGISTRATION_ROUTING_KEY = "user.registration";

    // Dead letter exchange
    public static final String DEAD_LETTER_EXCHANGE = "dead.letter.exchange";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter";

    // Enums for exchange and queue names
    public enum ExchangeNameEnum {
        NOTIFICATION_EXCHANGE(RabbitMQConfig.NOTIFICATION_EXCHANGE),
        REGISTRATION_EXCHANGE(RabbitMQConfig.REGISTRATION_EXCHANGE),
        DEAD_LETTER_EXCHANGE(RabbitMQConfig.DEAD_LETTER_EXCHANGE);

        private final String name;

        ExchangeNameEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum QueueNameEnum {
        EMAIL_NOTIFICATION_QUEUE(RabbitMQConfig.EMAIL_NOTIFICATION_QUEUE),
        USER_REGISTRATION_QUEUE(RabbitMQConfig.USER_REGISTRATION_QUEUE),
        DEAD_LETTER_QUEUE(RabbitMQConfig.DEAD_LETTER_QUEUE);

        private final String name;

        QueueNameEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum RoutingKeyEnum {
        EMAIL_NOTIFICATION_ROUTING_KEY(RabbitMQConfig.EMAIL_NOTIFICATION_ROUTING_KEY),
        USER_REGISTRATION_ROUTING_KEY(RabbitMQConfig.USER_REGISTRATION_ROUTING_KEY),
        DEAD_LETTER_ROUTING_KEY(RabbitMQConfig.DEAD_LETTER_ROUTING_KEY);

        private final String name;

        RoutingKeyEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
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
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, RetryOperationsInterceptor retryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setAdviceChain(retryInterceptor);
        return factory;
    }

    // Dead Letter Exchange
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    // Notification Exchange and Queue
    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue emailNotificationQueue() {
        return QueueBuilder.durable(EMAIL_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding emailNotificationBinding() {
        return BindingBuilder.bind(emailNotificationQueue())
                .to(notificationExchange())
                .with(EMAIL_NOTIFICATION_ROUTING_KEY);
    }

    // Registration Exchange and Queue
    @Bean
    public DirectExchange registrationExchange() {
        return new DirectExchange(REGISTRATION_EXCHANGE);
    }

    @Bean
    public Queue userRegistrationQueue() {
        return QueueBuilder.durable(USER_REGISTRATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding userRegistrationBinding() {
        return BindingBuilder.bind(userRegistrationQueue())
                .to(registrationExchange())
                .with(USER_REGISTRATION_ROUTING_KEY);
    }
}
