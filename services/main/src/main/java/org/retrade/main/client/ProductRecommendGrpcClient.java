package org.retrade.main.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.retrade.proto.recommendation.ProductRequest;
import org.retrade.proto.recommendation.RecommendationServiceGrpc;
import org.retrade.proto.recommendation.ScoreItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ProductRecommendGrpcClient {
    @Value("${grpc.client.recommendation-service.host}")
    private String recommendServiceHost;
    @Value("${grpc.client.recommendation-service.port}")
    private int recommendServicePort;
    private ManagedChannel channel;
    private RecommendationServiceGrpc.RecommendationServiceBlockingStub serviceStub;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(recommendServiceHost, recommendServicePort)
                .usePlaintext()
                .build();
        serviceStub = RecommendationServiceGrpc.newBlockingStub(channel);
        log.info("Product recommendation gRPC client initialized for {}:{}", recommendServiceHost, recommendServicePort);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                log.info("Product recommendation gRPC client shutdown completed");
            } catch (InterruptedException e) {
                log.warn("Product recommendation gRPC client shutdown interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public List<String> getSimilarProductByProductId(String productId, int page, int pageSize) {
        ProductRequest request = ProductRequest.newBuilder()
                .setProductId(productId)
                .setPage(page + 1)
                .setPageSize(pageSize)
                .build();
        var productResponse = serviceStub.recommendByProductId(request);
        return productResponse.getItemsList().stream().map(ScoreItem::getId).toList();
    }

    public List<String> getSimilarProductByProductIds(Set<String> productIds, int page, int pageSize) {
        ProductRequest request = ProductRequest.newBuilder()
                .addAllProductIds(productIds)
                .setPage(page + 1)
                .setPageSize(pageSize)
                .build();
        var productResponse = serviceStub.listSimilar(request);
        return productResponse.getItemsList().stream().map(ScoreItem::getId).toList();
    }
}
