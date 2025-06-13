package org.retrade.main.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.message.MessageObject;
import org.retrade.main.model.message.CCCDVerificationResultMessage;
import org.retrade.main.service.MessageProducerService;
import org.retrade.main.service.SellerService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CCCDVerifiedConsumer {
    private final ObjectMapper objectMapper;
    private final MessageProducerService messageProducerService;
    private final SellerService sellerService;
    @RabbitListener(queues = "identity.verified.result.queue")
    public void handleCCCDVerified(Message rawMessage, Channel channel) throws IOException {
        long deliveryTag = rawMessage.getMessageProperties().getDeliveryTag();
        try {
            byte[] body = rawMessage.getBody();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MessageObject<CCCDVerificationResultMessage> wrapper = objectMapper.readValue(
                    body,
                    new TypeReference<>() {}
            );
            var message = wrapper.getPayload();
            sellerService.updateVerifiedSeller(message);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            channel.basicNack(deliveryTag, false, false);
        }
    }

}
