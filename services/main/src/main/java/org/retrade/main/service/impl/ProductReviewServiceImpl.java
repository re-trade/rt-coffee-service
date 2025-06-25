package org.retrade.main.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.CreateProductReviewRequest;
import org.retrade.main.model.dto.request.UpdateProductReviewRequest;
import org.retrade.main.model.dto.response.ProductReviewResponse;
import org.retrade.main.model.entity.OrderComboEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.ProductReviewEntity;
import org.retrade.main.repository.OrderComboRepository;
import org.retrade.main.repository.ProductRepository;
import org.retrade.main.repository.ProductReviewRepository;
import org.retrade.main.service.ProductReviewService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository productReviewRepository;
    private final AuthUtils authUtils;
    private final ProductRepository productRepository;
    private final OrderComboRepository orderComboRepository;
    @Override
    public ProductReviewResponse createProductReview(CreateProductReviewRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        var customer = account.getCustomer();
        OrderComboEntity orderComboEntity = orderComboRepository.findById(request.getOrderId()).orElseThrow(
                () -> new ValidationException("Order combo not found")
        );
        ProductEntity productEntity = productRepository.findById(request.getProductId()).orElseThrow(
                () -> new ValidationException("Product not found")
        );
        boolean containsProduct = orderComboEntity.getOrderItems().stream()
                .anyMatch(orderItem -> orderItem.getProduct().getId().equals(productEntity.getId()));
        if (!containsProduct) {
            throw new ValidationException("Order does not contain product id: " + request.getProductId());
        }
        ProductReviewEntity productReviewEntity = new ProductReviewEntity();
        productReviewEntity.setCustomer(customer);
        productReviewEntity.setProduct(productEntity);
        productReviewEntity.setVote(request.getVote());
        productReviewEntity.setContent(request.getContent());
        productReviewEntity.setStatus(true);
        productReviewEntity.setOrder(orderComboEntity);
        try {
            productReviewRepository.save(productReviewEntity);
            return ProductReviewResponse.builder()
                    .id(productReviewEntity.getId())
                    .createdAt(productReviewEntity.getCreatedDate().toLocalDateTime())
                    .updatedAt(productReviewEntity.getUpdatedDate().toLocalDateTime())
                    .vote(productReviewEntity.getVote())
                    .content(productReviewEntity.getContent())
                    .authorId(productReviewEntity.getCustomer().getId())
                    .orderId(productReviewEntity.getOrder().getId())
                    .status(productReviewEntity.getStatus())
                    .productId(productReviewEntity.getProduct().getId())
                    .build();
        }catch (Exception e) {
            throw new ActionFailedException("Product review could not be saved " + e.getMessage());
        }

    }

    @Override
    public PaginationWrapper<List<ProductReviewResponse>> getProductReviewByProductId(String productId, QueryWrapper queryWrapper) {
        return null;
    }

    @Override
    public ProductReviewResponse getProductReviewDetails(String id) {
        return null;
    }

    @Override
    public ProductReviewResponse updateProductReview(String id, UpdateProductReviewRequest request) {
        return null;
    }

    @Override
    public ProductReviewResponse deleteProductReview(String id) {
        return null;
    }
}
