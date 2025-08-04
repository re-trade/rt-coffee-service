package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.request.CreateProductReviewRequest;
import org.retrade.main.model.dto.request.UpdateProductReviewRequest;
import org.retrade.main.model.dto.response.ProductOrderNoReview;
import org.retrade.main.model.dto.response.ProductReviewResponse;
import org.retrade.main.model.dto.response.ReviewStatsResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ProductReviewService {
    ProductReviewResponse createProductReview(CreateProductReviewRequest request);

    PaginationWrapper <List<ProductReviewResponse>> getProductReviewByProductId(String productId, QueryWrapper queryWrapper);

    ProductReviewResponse getProductReviewDetails(String id);

    ProductReviewResponse updateProductReview(String id, UpdateProductReviewRequest request);

    ProductReviewResponse deleteProductReview(String id);

    PaginationWrapper <List<ProductReviewResponse>> getProductReviewBySellerId(String sellerId, QueryWrapper queryWrapper);

    PaginationWrapper <List<ProductReviewResponse>>  geAllProductReviewBySeller(QueryWrapper queryWrapper);

    ProductReviewResponse createReplyProductReview(String id, String content);

    ProductReviewResponse updateReplyProductReview(String id, String content);

    ReviewStatsResponse getStatsSeller();

    PaginationWrapper <List<ProductReviewResponse>> getAllProductReviewsBySellerAndSearch(Double vote,String isReply, QueryWrapper queryWrapper);

    Long totalReviewByProductId(String productId);

    PaginationWrapper <List<ProductOrderNoReview>> getAllProductNoReviewByCustomer(QueryWrapper queryWrapper);
}
