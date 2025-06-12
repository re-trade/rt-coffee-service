package org.retrade.prover.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.message.MessageObject;
import org.retrade.prover.config.RabbitMQConfig;
import org.retrade.prover.model.message.CCCDVerificationMessage;
import org.retrade.prover.model.message.CCCDVerificationResultMessage;
import org.retrade.prover.service.CCCDProverService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CCCDProverConsumer {
    private final ObjectMapper objectMapper;
    private final CCCDProverService cccdProverService;
    private final RabbitTemplate rabbitTemplate;
    @RabbitListener(queues = "identity.verification.queue")
    public void consume(Message rawMessage, Channel channel) throws IOException {
        long deliveryTag = rawMessage.getMessageProperties().getDeliveryTag();
        try {
            byte[] body = rawMessage.getBody();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MessageObject<CCCDVerificationMessage> wrapper = objectMapper.readValue(
                    body,
                    new TypeReference<>() {}
            );
            var message = wrapper.getPayload();
            var result = cccdProverService.processVerification(message);
            var resultWrapper = CCCDVerificationResultMessage.builder()
                    .accepted(result.getValid())
                    .sellerId(message.getSellerId())
                    .message(result.getMessage())
                    .build();
            var messageWrapper = new MessageObject.Builder<CCCDVerificationResultMessage>()
                    .withMessageId(message.getMessageId())
                    .withSource("prover-service")
                    .withType("verification")
                    .withPayload(resultWrapper)
                    .withTimestamp(LocalDateTime.now())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.ExchangeNameEnum.IDENTITY_EXCHANGE.getName(),
                    RabbitMQConfig.RoutingKeyEnum.IDENTITY_VERIFICATION_ROUTING_KEY.getName(),
                    messageWrapper);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
