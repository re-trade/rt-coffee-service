package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.CreateProductReviewRequest;
import org.retrade.main.model.dto.request.UpdateProductReviewRequest;
import org.retrade.main.model.dto.response.ProductReviewBaseResponse;
import org.retrade.main.model.dto.response.ProductReviewResponse;
import org.retrade.main.service.ProductReviewService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("product-review")
@RequiredArgsConstructor
public class ProductReviewController {
    private final ProductReviewService productReviewService;

    @PostMapping
    public ResponseEntity<ResponseObject<ProductReviewBaseResponse>> createProductReview(@RequestBody CreateProductReviewRequest request) {
        var result = productReviewService.createProductReview(request);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewBaseResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Create product review success")
                .build());
    }

    @GetMapping
    public ResponseEntity<ResponseObject<List<ProductReviewResponse>>> getProductReviews(
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();
        var result = productReviewService.geAllProductReviewBySeller(queryWrapper);

        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductReviewResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Get product review success")
                .build());
    }

    @GetMapping("product/{productId}")
    public ResponseEntity<ResponseObject<List<ProductReviewBaseResponse>>> getProductReviewByProductId(
            @PathVariable String productId,
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();
        var result = productReviewService.getProductReviewByProductId(productId,queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductReviewBaseResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("get product review success")
                .build());
    }
    @GetMapping("{id}")
    public ResponseEntity<ResponseObject<ProductReviewBaseResponse>> getProductReviewDetails(@PathVariable String id){
        var result = productReviewService.getProductReviewDetails(id);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewBaseResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Get product details success")
                .build());

    }
    @PutMapping("{id}")
    public ResponseEntity<ResponseObject<ProductReviewBaseResponse>> updateProductReview(
            @PathVariable String id,
            @RequestBody UpdateProductReviewRequest request){
        var result = productReviewService.updateProductReview(id,request);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewBaseResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Update product review success")
                .build());
    }
    @DeleteMapping("{id}")
    public ResponseEntity<ResponseObject<ProductReviewBaseResponse>> deleteProductReview(@PathVariable String id){
        var result = productReviewService.deleteProductReview(id);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewBaseResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Delete product review success")
                .build());
    }
    @GetMapping("seller/{sellerId}")
    public ResponseEntity<ResponseObject<List<ProductReviewBaseResponse>>> getAllShopReview(
            @PathVariable String sellerId,
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();
        var result = productReviewService.getProductReviewBySellerId(sellerId,queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductReviewBaseResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Create product review success")
                .build());
    }
}
