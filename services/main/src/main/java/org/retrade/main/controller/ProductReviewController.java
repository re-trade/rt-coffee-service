package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.CreateProductReviewRequest;
import org.retrade.main.model.dto.request.ProductReviewReplyRequest;
import org.retrade.main.model.dto.request.UpdateProductReviewRequest;
import org.retrade.main.model.dto.response.ProductOrderNoReview;
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
                .messages("Tạo đánh giá sản phẩm thành công")
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
                .messages("Lấy danh sách đánh giá sản phẩm thành công")
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
                .messages("Lấy danh sách đánh giá sản phẩm thành công")
                .build());
    }
    @GetMapping("{id}")
    public ResponseEntity<ResponseObject<ProductReviewResponse>> getProductReviewDetails(@PathVariable String id){
        var result = productReviewService.getProductReviewDetails(id);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Lấy chi tiết đánh giá sản phẩm thành công")
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
                .messages("Cập nhật đánh giá sản phẩm thành công")
                .build());
    }
    @DeleteMapping("{id}")
    public ResponseEntity<ResponseObject<ProductReviewResponse>> deleteProductReview(@PathVariable String id){
        var result = productReviewService.deleteProductReview(id);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Xóa đánh giá sản phẩm thành công")
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
                .messages("Lấy đánh giá sản phẩm thành công")
                .build());
    }
    @PatchMapping("{id}/create-reply")
    public ResponseEntity<ResponseObject<ProductReviewResponse>> createReplyProductReview(@PathVariable String id, @RequestBody ProductReviewReplyRequest request){
        var result = productReviewService.createReplyProductReview(id,request.content());
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Phản hồi đánh giá sản phẩm thành công")
                .build());
    }

    @PatchMapping("{id}/update-reply")
    public ResponseEntity<ResponseObject<ProductReviewResponse>> updateReplyProductReview(@PathVariable String id, @RequestBody ProductReviewReplyRequest request){
        var result = productReviewService.updateReplyProductReview(id, request.content());
        return ResponseEntity.ok(new ResponseObject.Builder<ProductReviewResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Cập nhật phản hồi đánh giá thành công")
                .build());
    }
    @GetMapping("stats")
    public ResponseEntity<ResponseObject<ReviewStatsResponse>> getStatsSeller(){
        var result = productReviewService.getStatsSeller();
        return ResponseEntity.ok(new ResponseObject.Builder<ReviewStatsResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Lấy thống kê đánh giá cửa hàng thành công")
                .build());

    }

    @GetMapping("search")
    public ResponseEntity<ResponseObject<List<ProductReviewResponse>>> getAllProductReviewsBySellerAndSearch(
            @RequestParam(required = false, name = "q") String search,
            @RequestParam(required = false) Double vote,
            @RequestParam(required = false) String isReply,
            @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = productReviewService.getAllProductReviewsBySellerAndSearch(vote,isReply,queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductReviewResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Lấy đánh giá sản phẩm thành công")
                .build());
    }

    @GetMapping("product/{productId}/count")
    public ResponseEntity<ResponseObject<Long>> getProductReviewCount(@PathVariable String productId){
        Long result = productReviewService.totalReviewByProductId(productId);
        return ResponseEntity.ok(new ResponseObject.Builder<Long>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Lấy tổng số đánh giá sản phẩm thành công")
                .build());
    }
    @GetMapping("no-review")
    public ResponseEntity<ResponseObject<List<ProductOrderNoReview>>> getAllProductNoReviewByCustomer(
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = productReviewService.getAllProductNoReviewByCustomer(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductOrderNoReview>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Lấy danh sách sản phẩm chưa đánh giá thành công")
                .build());
    }

    @GetMapping("my")
    public ResponseEntity<ResponseObject<List<ProductReviewResponse>>> getAllProductReviewByCustomer(
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = productReviewService.getAllProductReviewByCustomer(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductReviewResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Lấy danh sách sản phẩm chưa đánh giá thành công")
                .build());
    }

}
