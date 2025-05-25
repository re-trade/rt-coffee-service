package org.retrade.voucher.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.retrade.proto.product.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ProductGrpcClient {
    
    @Value("${grpc.client.main-service.host:localhost}")
    private String mainServiceHost;
    
    @Value("${grpc.client.main-service.port:9080}")
    private int mainServicePort;
    
    private ManagedChannel channel;
    private GrpcProductServiceGrpc.GrpcProductServiceBlockingStub productServiceStub;
    
    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress(mainServiceHost, mainServicePort)
                .usePlaintext()
                .build();
        productServiceStub = GrpcProductServiceGrpc.newBlockingStub(channel);
        log.info("Product gRPC client initialized for {}:{}", mainServiceHost, mainServicePort);
    }
    
    @PreDestroy
    public void destroy() {
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                log.info("Product gRPC client channel closed");
            } catch (InterruptedException e) {
                log.warn("Failed to close gRPC channel gracefully", e);
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public ProductInfo getProduct(String productId) {
        try {
            GetProductRequest request = GetProductRequest.newBuilder()
                    .setProductId(productId)
                    .build();
            
            GetProductResponse response = productServiceStub.getProduct(request);
            
            if (response.getSuccess()) {
                log.debug("Successfully retrieved product: {}", productId);
                return response.getProduct();
            } else {
                log.warn("Failed to get product {}: {}", productId, response.getMessage());
                return null;
            }
        } catch (Exception e) {
            log.error("Error calling product service for product {}: {}", productId, e.getMessage(), e);
            return null;
        }
    }
    
    public List<ProductInfo> getProducts(List<String> productIds) {
        try {
            GetProductsRequest request = GetProductsRequest.newBuilder()
                    .addAllProductIds(productIds)
                    .build();
            
            GetProductsResponse response = productServiceStub.getProducts(request);
            
            if (response.getSuccess()) {
                log.debug("Successfully retrieved {} products", response.getProductsList().size());
                return response.getProductsList();
            } else {
                log.warn("Failed to get products: {}", response.getMessage());
                return List.of();
            }
        } catch (Exception e) {
            log.error("Error calling product service for products {}: {}", productIds, e.getMessage(), e);
            return List.of();
        }
    }
    
    public List<ProductInfo> getProductsByCategory(String category, int page, int size) {
        try {
            GetProductsByCategoryRequest request = GetProductsByCategoryRequest.newBuilder()
                    .setCategory(category)
                    .setPage(page)
                    .setSize(size)
                    .build();
            
            GetProductsByCategoryResponse response = productServiceStub.getProductsByCategory(request);
            
            if (response.getSuccess()) {
                log.debug("Successfully retrieved {} products for category: {}", response.getProductsList().size(), category);
                return response.getProductsList();
            } else {
                log.warn("Failed to get products by category {}: {}", category, response.getMessage());
                return List.of();
            }
        } catch (Exception e) {
            log.error("Error calling product service for category {}: {}", category, e.getMessage(), e);
            return List.of();
        }
    }
    
    public List<ProductInfo> getProductsBySeller(String sellerId, int page, int size) {
        try {
            GetProductsBySellerRequest request = GetProductsBySellerRequest.newBuilder()
                    .setSellerId(sellerId)
                    .setPage(page)
                    .setSize(size)
                    .build();
            
            GetProductsBySellerResponse response = productServiceStub.getProductsBySeller(request);
            
            if (response.getSuccess()) {
                log.debug("Successfully retrieved {} products for seller: {}", response.getProductsList().size(), sellerId);
                return response.getProductsList();
            } else {
                log.warn("Failed to get products by seller {}: {}", sellerId, response.getMessage());
                return List.of();
            }
        } catch (Exception e) {
            log.error("Error calling product service for seller {}: {}", sellerId, e.getMessage(), e);
            return List.of();
        }
    }
    
    public ValidateProductsResponse validateProducts(List<String> productIds) {
        try {
            ValidateProductsRequest request = ValidateProductsRequest.newBuilder()
                    .addAllProductIds(productIds)
                    .build();
            
            ValidateProductsResponse response = productServiceStub.validateProducts(request);
            
            if (response.getSuccess()) {
                log.debug("Successfully validated products: {} valid, {} invalid", 
                    response.getValidProductIdsList().size(), 
                    response.getInvalidProductIdsList().size());
            } else {
                log.warn("Failed to validate products: {}", response.getMessage());
            }
            
            return response;
        } catch (Exception e) {
            log.error("Error validating products {}: {}", productIds, e.getMessage(), e);
            return ValidateProductsResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to validate products: " + e.getMessage())
                    .build();
        }
    }
}
