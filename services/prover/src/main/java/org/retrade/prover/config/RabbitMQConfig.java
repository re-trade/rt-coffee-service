package org.retrade.prover.config;

import lombok.Getter;
import org.springframework.amqp.core.AcknowledgeMode;
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
        IDENTITY_EXCHANGE("identity.exchange"),
        IDENTITY_RETRY_EXCHANGE("identity.dlx.exchange");
        private final String name;
        ExchangeNameEnum(String name) {
            this.name = name;
        }
    }

    @Getter
    public enum QueueNameEnum {
        IDENTITY_VERIFICATION_QUEUE("identity.verification.queue"),
        IDENTITY_VERIFIED_RESULT_QUEUE("identity.verified.result.queue"),
        IDENTITY_RETRY_QUEUE("identity.dlx.queue");
        private final String name;
        QueueNameEnum(String name) {
            this.name = name;
        }
    }

    @Getter
    public enum RoutingKeyEnum {
        IDENTITY_VERIFICATION_ROUTING_KEY("identity.verification"),
        IDENTITY_VERIFIED_ROUTING_KEY("identity.verified"),
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
}
