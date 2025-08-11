package org.retrade.feedback_notification.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.proto.authentication.AccountIdRequest;
import org.retrade.proto.authentication.GetAccountResponse;
import org.retrade.proto.authentication.GrpcTokenServiceGrpc;
import org.retrade.proto.authentication.UsernameRequest;
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

    public GetAccountResponse getAccountInfoByUsername(String username) {
        return blockingStub.getUserAccountByUserName(UsernameRequest.newBuilder()
                        .setUsername(username)
                .build());
    }

    public GetAccountResponse getAccountInfoById(String id) {
        return blockingStub.getUserAccountByAccountId(AccountIdRequest.newBuilder()
                        .setId(id)
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
