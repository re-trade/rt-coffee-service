package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.request.CreateProductReviewRequest;
import org.retrade.main.model.dto.request.UpdateProductReviewRequest;
import org.retrade.main.model.dto.response.ProductReviewBaseResponse;
import org.retrade.main.model.dto.response.ProductReviewResponse;

import java.util.List;

public interface ProductReviewService {
    ProductReviewBaseResponse createProductReview(CreateProductReviewRequest request);

    PaginationWrapper <List<ProductReviewBaseResponse>> getProductReviewByProductId(String productId, QueryWrapper queryWrapper);

    ProductReviewBaseResponse getProductReviewDetails(String id);

    ProductReviewBaseResponse updateProductReview(String id, UpdateProductReviewRequest request);

    ProductReviewBaseResponse deleteProductReview(String id);

    PaginationWrapper <List<ProductReviewBaseResponse>> getProductReviewBySellerId(String sellerId, QueryWrapper queryWrapper);

    PaginationWrapper <List<ProductReviewResponse>>  geAllProductReviewBySeller(QueryWrapper queryWrapper);
}
