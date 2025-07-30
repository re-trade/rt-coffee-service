package org.retrade.achievement.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.retrade.proto.CountCompletedOrdersRequest;
import org.retrade.proto.OrderServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderServiceClient {
    @Value("${grpc.client.main-service.host:localhost}")
    private String mainServiceHost;

    @Value("${grpc.client.main-service.port:9080}")
    private int mainServicePort;

    private ManagedChannel channel;
    private OrderServiceGrpc.OrderServiceBlockingStub blockingStub;

    public long getOrderCount(String sellerId) {
        var result = blockingStub.countCompletedOrders(CountCompletedOrdersRequest.newBuilder()
                        .setSellerId(sellerId)
                .build());
        if (result.getSuccess()) {
            return result.getTotalOrders();
        }
        else {
            log.error("Failed to get order count for seller {} with message {}", sellerId, result.getMessage());
            return 0;
        }
    }

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(mainServiceHost, mainServicePort)
                .usePlaintext()
                .build();
        blockingStub = OrderServiceGrpc.newBlockingStub(channel);
        log.info("Order gRPC client initialized for {}:{}", mainServiceHost, mainServicePort);
    }

    @PreDestroy
    public void destroy() {
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
                log.info("Order gRPC client channel closed");
            } catch (InterruptedException e) {
                log.warn("Failed to close gRPC channel gracefully", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
