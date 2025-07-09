package org.retrade.main.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.main.model.dto.response.ProductResponse;
import org.retrade.main.service.ProductService;
import org.retrade.proto.product.*;
import org.springframework.grpc.server.service.GrpcService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class ProductGrpcServiceImpl extends GrpcProductServiceGrpc.GrpcProductServiceImplBase {
    private final ProductService productService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public void getProduct(GetProductRequest request, StreamObserver<GetProductResponse> responseObserver) {
        try {
            log.info("Getting product with ID: {}", request.getProductId());
            ProductResponse product = productService.getProductById(request.getProductId());
            
            ProductInfo productInfo = mapToProductInfo(product);
            
            GetProductResponse response = GetProductResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Product retrieved successfully")
                    .setProduct(productInfo)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Product retrieved successfully: {}", request.getProductId());
            
        } catch (Exception e) {
            log.error("Error getting product: {}", e.getMessage(), e);
            GetProductResponse response = GetProductResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to get product: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getProducts(GetProductsRequest request, StreamObserver<GetProductsResponse> responseObserver) {
        try {
            log.info("Getting products with IDs: {}", request.getProductIdsList());
            List<ProductInfo> products = request.getProductIdsList().stream()
                    .map(productId -> {
                        try {
                            ProductResponse product = productService.getProductById(productId);
                            return mapToProductInfo(product);
                        } catch (Exception e) {
                            log.warn("Failed to get product {}: {}", productId, e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            GetProductsResponse response = GetProductsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Products retrieved successfully")
                    .addAllProducts(products)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Retrieved {} products successfully", products.size());
            
        } catch (Exception e) {
            log.error("Error getting products: {}", e.getMessage(), e);
            GetProductsResponse response = GetProductsResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to get products: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getProductsByCategory(GetProductsByCategoryRequest request, StreamObserver<GetProductsByCategoryResponse> responseObserver) {
        try {
            log.info("Getting products by category: {}", request.getCategory());

            String searchQuery = "categories=" + request.getCategory();
            var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                    .search(searchQuery)
                    .build();
            
            var paginatedProducts = productService.getAllProducts(queryWrapper);
            
            List<ProductInfo> products = paginatedProducts.getData().stream()
                    .map(this::mapToProductInfo)
                    .collect(Collectors.toList());
            
            GetProductsByCategoryResponse response = GetProductsByCategoryResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Products retrieved successfully")
                    .addAllProducts(products)
                    .setTotalPages(paginatedProducts.getTotalPages())
                    .setTotalElements(paginatedProducts.getTotalElements())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Retrieved {} products for category: {}", products.size(), request.getCategory());
            
        } catch (Exception e) {
            log.error("Error getting products by category: {}", e.getMessage(), e);
            GetProductsByCategoryResponse response = GetProductsByCategoryResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to get products by category: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getProductsBySeller(GetProductsBySellerRequest request, StreamObserver<GetProductsBySellerResponse> responseObserver) {
        try {
            log.info("Getting products by seller: {}", request.getSellerId());

            String searchQuery = "sellerId=" + request.getSellerId();
            var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                    .search(searchQuery)
                    .build();
            
            var paginatedProducts = productService.getAllProducts(queryWrapper);
            
            List<ProductInfo> products = paginatedProducts.getData().stream()
                    .map(this::mapToProductInfo)
                    .collect(Collectors.toList());
            
            GetProductsBySellerResponse response = GetProductsBySellerResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Products retrieved successfully")
                    .addAllProducts(products)
                    .setTotalPages(paginatedProducts.getTotalPages())
                    .setTotalElements(paginatedProducts.getTotalElements())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Retrieved {} products for seller: {}", products.size(), request.getSellerId());
            
        } catch (Exception e) {
            log.error("Error getting products by seller: {}", e.getMessage(), e);
            GetProductsBySellerResponse response = GetProductsBySellerResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to get products by seller: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void validateProducts(ValidateProductsRequest request, StreamObserver<ValidateProductsResponse> responseObserver) {
        try {
            log.info("Validating products: {}", request.getProductIdsList());
            
            List<String> validProductIds = request.getProductIdsList().stream()
                    .filter(productId -> {
                        try {
                            productService.getProductById(productId);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            
            List<String> invalidProductIds = request.getProductIdsList().stream()
                    .filter(productId -> !validProductIds.contains(productId))
                    .collect(Collectors.toList());
            
            ValidateProductsResponse response = ValidateProductsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Product validation completed")
                    .addAllValidProductIds(validProductIds)
                    .addAllInvalidProductIds(invalidProductIds)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Validated products: {} valid, {} invalid", validProductIds.size(), invalidProductIds.size());
            
        } catch (Exception e) {
            log.error("Error validating products: {}", e.getMessage(), e);
            ValidateProductsResponse response = ValidateProductsResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to validate products: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private ProductInfo mapToProductInfo(ProductResponse product) {
        ProductInfo.Builder builder = ProductInfo.newBuilder()
                .setId(product.getId())
                .setName(product.getName())
                .setSellerId(product.getSellerId())
                .setSellerShopName(product.getSellerShopName())
                .setShortDescription(product.getShortDescription() != null ? product.getShortDescription() : "")
                .setDescription(product.getDescription() != null ? product.getDescription() : "")
                .setThumbnail(product.getThumbnail() != null ? product.getThumbnail() : "")
                .setBrand(product.getBrand())
                .setModel(product.getModel())
                .setCurrentPrice(product.getCurrentPrice().doubleValue())
                .setVerified(product.getVerified() != null ? product.getVerified() : false);

        if (product.getProductImages() != null) {
            builder.addAllProductImages(product.getProductImages());
        }
        if (product.getTags() != null) {
            builder.addAllTags(product.getTags());
        }
        if (product.getCreatedAt() != null) {
            builder.setCreatedAt(product.getCreatedAt().format(DATE_FORMATTER));
        }
        if (product.getUpdatedAt() != null) {
            builder.setUpdatedAt(product.getUpdatedAt().format(DATE_FORMATTER));
        }
        return builder.build();
    }
}
