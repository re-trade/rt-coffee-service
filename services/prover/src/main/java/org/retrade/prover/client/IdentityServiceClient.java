package org.retrade.prover.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.retrade.proto.identity.IdentityCardRequest;
import org.retrade.proto.identity.IdentityCardResponse;
import org.retrade.proto.identity.IdentityServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IdentityServiceClient {
    @Value("${grpc.client.id-card-extractor.host:localhost}")
    private String mainServiceHost;

    @Value("${grpc.client.id-card-extractor.port:5000}")
    private int mainServicePort;

    private ManagedChannel channel;
    private IdentityServiceGrpc.IdentityServiceBlockingStub blockingStub;


    public IdentityCardResponse getCardByBase64Image (String base64Image) {
        return blockingStub.processIdentityCard(IdentityCardRequest.newBuilder()
                        .setBase64Image(base64Image)
                .build());
    }

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(mainServiceHost, mainServicePort)
                .usePlaintext()
                .build();
        blockingStub = IdentityServiceGrpc.newBlockingStub(channel);
        log.info("Identity gRPC client initialized for {}:{}", mainServiceHost, mainServicePort);
    }

    @PreDestroy
    public void destroy() {
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
                log.info("Identity gRPC client channel closed");
            } catch (InterruptedException e) {
                log.warn("Failed to close gRPC channel gracefully", e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
