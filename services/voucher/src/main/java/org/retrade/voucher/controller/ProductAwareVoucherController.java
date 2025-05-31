package org.retrade.voucher.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.proto.product.ProductInfo;
import org.retrade.voucher.model.dto.request.CreateProductAwareVoucherRequest;
import org.retrade.voucher.model.dto.response.ProductAwareVoucherResponse;
import org.retrade.voucher.service.ProductAwareVoucherService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("product-aware-vouchers")
@RequiredArgsConstructor
public class ProductAwareVoucherController {
    private final ProductAwareVoucherService productAwareVoucherService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<ProductAwareVoucherResponse>> createProductAwareVoucher(
            @Valid @RequestBody CreateProductAwareVoucherRequest request) {
        var result = productAwareVoucherService.createProductAwareVoucher(request);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductAwareVoucherResponse>()
                .success(true)
                .code("VOUCHER_CREATED")
                .content(result)
                .messages("Product-aware voucher created successfully")
                .build());
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseObject<ProductAwareVoucherResponse>> getProductAwareVoucherById(
            @PathVariable String id) {
        var result = productAwareVoucherService.getProductAwareVoucherById(id);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductAwareVoucherResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Voucher retrieved successfully")
                .build());
    }

    @GetMapping("code/{code}")
    public ResponseEntity<ResponseObject<ProductAwareVoucherResponse>> getProductAwareVoucherByCode(
            @PathVariable String code) {
        var result = productAwareVoucherService.getProductAwareVoucherByCode(code);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductAwareVoucherResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Voucher retrieved successfully")
                .build());
    }

    @GetMapping
    public ResponseEntity<ResponseObject<List<ProductAwareVoucherResponse>>> getAllProductAwareVouchers(
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {
        
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();
        
        var result = productAwareVoucherService.getProductAwareVouchers(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductAwareVoucherResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Vouchers retrieved successfully")
                .build());
    }

    @GetMapping("product/{productId}")
    public ResponseEntity<ResponseObject<List<ProductAwareVoucherResponse>>> getVouchersForProduct(
            @PathVariable String productId) {
        var result = productAwareVoucherService.getVouchersForProduct(productId);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductAwareVoucherResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Vouchers for product retrieved successfully")
                .build());
    }

    @GetMapping("category/{category}")
    public ResponseEntity<ResponseObject<List<ProductAwareVoucherResponse>>> getVouchersForCategory(
            @PathVariable String category) {
        var result = productAwareVoucherService.getVouchersForCategory(category);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductAwareVoucherResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Vouchers for category retrieved successfully")
                .build());
    }

    @GetMapping("seller/{sellerId}")
    public ResponseEntity<ResponseObject<List<ProductAwareVoucherResponse>>> getVouchersForSeller(
            @PathVariable String sellerId) {
        var result = productAwareVoucherService.getVouchersForSeller(sellerId);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductAwareVoucherResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Vouchers for seller retrieved successfully")
                .build());
    }

    @GetMapping("{code}/applicable-products")
    public ResponseEntity<ResponseObject<List<ProductInfo>>> getApplicableProducts(
            @PathVariable String code) {
        var result = productAwareVoucherService.getApplicableProducts(code);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductInfo>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Applicable products retrieved successfully")
                .build());
    }

    @GetMapping("{code}/check-applicability/{productId}")
    public ResponseEntity<ResponseObject<Boolean>> checkVoucherApplicability(
            @PathVariable String code,
            @PathVariable String productId) {
        var result = productAwareVoucherService.isVoucherApplicableToProduct(code, productId);
        return ResponseEntity.ok(new ResponseObject.Builder<Boolean>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Voucher applicability checked successfully")
                .build());
    }

    @PostMapping("{code}/check-applicability-bulk")
    public ResponseEntity<ResponseObject<Boolean>> checkVoucherApplicabilityBulk(
            @PathVariable String code,
            @RequestBody List<String> productIds) {
        var result = productAwareVoucherService.isVoucherApplicableToProducts(code, productIds);
        return ResponseEntity.ok(new ResponseObject.Builder<Boolean>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Bulk voucher applicability checked successfully")
                .build());
    }
}
