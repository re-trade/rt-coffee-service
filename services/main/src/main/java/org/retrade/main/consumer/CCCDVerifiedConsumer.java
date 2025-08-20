package org.retrade.main.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.common.model.message.MessageObject;
import org.retrade.main.model.message.CCCDVerificationResultMessage;
import org.retrade.main.model.message.EmailNotificationMessage;
import org.retrade.main.service.MessageProducerService;
import org.retrade.main.service.SellerService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
            var sellerProfile = sellerService.getSellerBaseInfoById(message.getSellerId());
            if (sellerProfile.isEmpty()) {
                log.error("No seller profile found for verified CCCD message");
                channel.basicNack(deliveryTag, false, false);
                return;
            }
            try {
                sellerService.updateVerifiedSeller(message);
            } catch (ValidationException ex) {
                messageProducerService.sendEmailNotification(EmailNotificationMessage.builder()
                                .to(sellerProfile.get().email())
                                .subject("SELLER-VERIFIED-FAILED")
                                .templateName("seller-verified-failed")
                                .retryCount(0)
                                .messageId(UUID.randomUUID().toString())
                        .build());
            }

            channel.basicAck(deliveryTag, false);
            log.info("Successfully verified CCCD verified message");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            handleRetry(channel, deliveryTag, getRetryCountFromXDeath(rawMessage), rawMessage);
        }
    }

    private void handleRetry(Channel channel, long deliveryTag, int retryCount, Message message) throws IOException {
        if (retryCount >= 3) {
            log.warn("Max retry reached ({}), sending to error queue", retryCount);
            channel.basicAck(deliveryTag, false);
            messageProducerService.sendMessageToDeadQueue(message);
        } else {
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private int getRetryCountFromXDeath(Message message) {
        List<Map<String, ?>> xDeathHeader = message.getMessageProperties().getXDeathHeader();
        if (xDeathHeader != null && !xDeathHeader.isEmpty()) {
            Map<String, ?> deathEntry = xDeathHeader.getFirst();
            Object count = deathEntry.get("count");
            if (count instanceof Long) {
                return ((Long) count).intValue();
            } else if (count instanceof Integer) {
                return (Integer) count;
            }
        }
        return 0;
    }
}
