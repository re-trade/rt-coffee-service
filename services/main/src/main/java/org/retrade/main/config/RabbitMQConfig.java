package org.retrade.main.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public enum ExchangeNameEnum {
        NOTIFICATION_EXCHANGE("notification.exchange"),
        REGISTRATION_EXCHANGE("registration.exchange");
        private final String name;
        ExchangeNameEnum(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }

    public enum QueueNameEnum {
        EMAIL_NOTIFICATION_QUEUE("email.notification.queue"),
        USER_REGISTRATION_QUEUE("user.registration.queue"),
        DEAD_LETTER_QUEUE("dead.letter.queue");
        private final String name;
        QueueNameEnum(String name) {
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
}
