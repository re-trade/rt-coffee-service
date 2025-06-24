package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.CreateProductRequest;
import org.retrade.main.model.dto.request.ProductReviewRequest;
import org.retrade.main.model.dto.response.ProductResponse;
import org.retrade.main.model.dto.response.ProductReviewResponse;
import org.retrade.main.service.ProductReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ratings")
@RequiredArgsConstructor
@Tag(name = "Review orders", description = "Customer review orders after complete")
public class ProductReviewController {
    private ProductReviewService productReviewService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<ProductReviewResponse>> reviewOrder(
            @Valid @RequestBody ProductReviewRequest request) {
        var result = productReviewService.addProductReview(request);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Product review created successfully")
                .build());
    }
}
