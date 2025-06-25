package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.request.CreateProductReviewRequest;
import org.retrade.main.model.dto.request.UpdateProductReviewRequest;
import org.retrade.main.model.dto.response.ProductReviewResponse;

import java.util.List;

public interface ProductReviewService {
    ProductReviewResponse createProductReview(CreateProductReviewRequest request);

    PaginationWrapper <List<ProductReviewResponse>> getProductReviewByProductId(String productId, QueryWrapper queryWrapper);

    ProductReviewResponse getProductReviewDetails(String id);

    ProductReviewResponse updateProductReview(String id, UpdateProductReviewRequest request);

    ProductReviewResponse deleteProductReview(String id);

    PaginationWrapper <List<ProductReviewResponse>> getProductReviewBySellerId(String sellerId, QueryWrapper queryWrapper);
}
