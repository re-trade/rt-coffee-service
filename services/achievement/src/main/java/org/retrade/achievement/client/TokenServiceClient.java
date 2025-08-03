package org.retrade.achievement.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.proto.authentication.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenServiceClient {
    @Value("${grpc.client.main-service.host:localhost}")
    private String mainServiceHost;

    @Value("${grpc.client.main-service.port:9080}")
    private int mainServicePort;

    private ManagedChannel channel;
    private GrpcTokenServiceGrpc.GrpcTokenServiceBlockingStub blockingStub;

    public VerifyTokenResponse verifyToken(String token, TokenType tokenType) {
        return blockingStub.verifyToken(TokenRequest.newBuilder()
                .setToken(token)
                .setType(tokenType)
                .build());
    }

    public GetSellerProfileResponse getSellerProfileByToken(String token, TokenType tokenType) {
        return blockingStub.getSellerProfile(TokenRequest.newBuilder()
                .setToken(token)
                .setType(tokenType)
                .build());
    }

    public GetSellerProfileResponse getSellerProfileBySellerId(String id) {
        return blockingStub.getSellerProfileBySellerId(AccountIdRequest.newBuilder().setId(id)
                .build());
    }

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(mainServiceHost, mainServicePort)
                .usePlaintext()
                .build();
        blockingStub = GrpcTokenServiceGrpc.newBlockingStub(channel);
        log.info("Main gRPC client initialized for {}:{}", mainServiceHost, mainServicePort);
    }

    @PreDestroy
    public void destroy() {
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                log.info("Main gRPC client channel closed");
            } catch (InterruptedException e) {
                log.warn("Failed to close gRPC channel gracefully", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
