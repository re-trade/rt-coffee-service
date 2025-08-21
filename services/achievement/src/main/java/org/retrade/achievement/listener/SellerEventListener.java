package org.retrade.achievement.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.achievement.model.message.AchievementMessage;
import org.retrade.achievement.service.AchievementEvaluatorService;
import org.retrade.achievement.service.MessageProducerService;
import org.retrade.common.model.message.MessageObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerEventListener {
    private final AchievementEvaluatorService achievementEvaluatorService;
    private final MessageProducerService messageProducerService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "achievement.seller.event.queue")
    public void handleSellerEvent(Message rawMessage, Channel channel) throws IOException {
        long deliveryTag = rawMessage.getMessageProperties().getDeliveryTag();
        try {
            byte[] body = rawMessage.getBody();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MessageObject<AchievementMessage> wrapper = objectMapper.readValue(
                    body,
                    new TypeReference<>() {}
            );
            var message = wrapper.getPayload();
            achievementEvaluatorService.evaluate(message.getSellerId(), message.getEventType());
            channel.basicAck(deliveryTag, false);
            log.info("Successfully message");
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
