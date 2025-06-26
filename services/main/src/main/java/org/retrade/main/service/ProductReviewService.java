package org.retrade.main.service;

import org.retrade.main.model.dto.request.ProductReviewRequest;
import org.retrade.main.model.dto.response.ProductReviewResponse;

public interface ProductReviewService {
    ProductReviewResponse addProductReview(ProductReviewRequest request);
}
