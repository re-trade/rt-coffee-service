package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.CreateProductReviewRequest;
import org.retrade.main.model.dto.request.UpdateProductReviewRequest;
import org.retrade.main.model.dto.response.ProductReviewResponse;
import org.retrade.main.model.dto.response.ReviewStatsResponse;
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
    public ResponseEntity<ResponseObject<ProductReviewResponse>> createProductReview(@RequestBody CreateProductReviewRequest request) {
        var result = productReviewService.createProductReview(request);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewResponse>()
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
    public ResponseEntity<ResponseObject<List<ProductReviewResponse>>> getProductReviewByProductId(
            @PathVariable String productId,
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();
        var result = productReviewService.getProductReviewByProductId(productId,queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductReviewResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("get product review success")
                .build());
    }
    @GetMapping("{id}")
    public ResponseEntity<ResponseObject<ProductReviewResponse>> getProductReviewDetails(@PathVariable String id){
        var result = productReviewService.getProductReviewDetails(id);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Get product details success")
                .build());

    }
    @PutMapping("{id}")
    public ResponseEntity<ResponseObject<ProductReviewResponse>> updateProductReview(
            @PathVariable String id,
            @RequestBody UpdateProductReviewRequest request){
        var result = productReviewService.updateProductReview(id,request);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Update product review success")
                .build());
    }
    @DeleteMapping("{id}")
    public ResponseEntity<ResponseObject<ProductReviewResponse>> deleteProductReview(@PathVariable String id){
        var result = productReviewService.deleteProductReview(id);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Delete product review success")
                .build());
    }
    @GetMapping("seller/{sellerId}")
    public ResponseEntity<ResponseObject<List<ProductReviewResponse>>> getAllShopReview(
            @PathVariable String sellerId,
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();
        var result = productReviewService.getProductReviewBySellerId(sellerId,queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductReviewResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Get product review success")
                .build());
    }
    @PatchMapping("{id}/create-reply")
    public ResponseEntity<ResponseObject<ProductReviewResponse>> createReplyProductReview(@PathVariable String id, @RequestParam String content){
        var result = productReviewService.createReplyProductReview(id,content);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Reply product review success")
                .build());
    }

    @PatchMapping("{id}/update-reply")
    public ResponseEntity<ResponseObject<ProductReviewResponse>> updateReplyProductReview(@PathVariable String id, @RequestParam String content){
        var result = productReviewService.updateReplyProductReview(id,content);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Update reply product review success")
                .build());
    }
    @GetMapping("search")
    public ResponseEntity<ResponseObject<List<ProductReviewResponse>>> getAllProductReviewsBySellerAndSearch(
            @RequestParam(required = false, name = "q") String search,
            @RequestParam(required = false) Double vote,
            @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = productReviewService.getAllProductReviewsBySellerAndSearch(vote,search,queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductReviewResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Get product review success")
                .build());
    }
    @GetMapping("stats")
    public ResponseEntity<ResponseObject<ReviewStatsResponse>> getStatsSeller(){
        var result = productReviewService.getStatsSeller();
        return ResponseEntity.ok(new ResponseObject.Builder<ReviewStatsResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Get stats seller review success")
                .build());

    }

}
