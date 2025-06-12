package org.retrade.prover.consumer;

import lombok.RequiredArgsConstructor;
import org.retrade.prover.service.CCCDProverService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CCCDProverConsumer {
    private final CCCDProverService cccdProverService;
    @RabbitListener(queues = "identity.verification.queue")
    public void consume(String message) {

    }
}
