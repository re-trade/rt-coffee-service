package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.service.RabbitMQHealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final RabbitMQHealthService rabbitMQHealthService;

    @GetMapping("/rabbitmq")
    public ResponseEntity<ResponseObject<Map<String, Object>>> checkRabbitMQHealth() {
        boolean isHealthy = rabbitMQHealthService.isRabbitMQHealthy();
        
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", isHealthy ? "UP" : "DOWN");
        healthStatus.put("service", "RabbitMQ");
        
        return ResponseEntity.ok(new ResponseObject.Builder<Map<String, Object>>()
                .success(isHealthy)
                .code(isHealthy ? "RABBITMQ_HEALTHY" : "RABBITMQ_UNHEALTHY")
                .messages(isHealthy ? "RabbitMQ is healthy" : "RabbitMQ is unhealthy")
                .content(healthStatus)
                .build());
    }
}
