package org.retrade.voucher.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.voucher.model.dto.response.ProductSimpleResponse;
import org.retrade.voucher.model.dto.response.VoucherSimpleResponse;
import org.retrade.voucher.service.ProductAwareVoucherService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("vouchers")
@RequiredArgsConstructor
public class ProductAwareVoucherController {
    private final ProductAwareVoucherService productAwareVoucherService;

    @GetMapping
    public ResponseEntity<ResponseObject<List<VoucherSimpleResponse>>> getAllVouchers(
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {

        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = productAwareVoucherService.getVouchersSimple(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<VoucherSimpleResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Vouchers retrieved successfully")
                .build());
    }

    @GetMapping("{code}")
    public ResponseEntity<ResponseObject<VoucherSimpleResponse>> getVoucherByCode(
            @PathVariable String code) {
        var result = productAwareVoucherService.getVoucherSimpleByCode(code);
        return ResponseEntity.ok(new ResponseObject.Builder<VoucherSimpleResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Voucher retrieved successfully")
                .build());
    }

    @GetMapping("product/{productId}")
    public ResponseEntity<ResponseObject<List<VoucherSimpleResponse>>> getVouchersForProduct(
            @PathVariable String productId) {
        var result = productAwareVoucherService.getVouchersSimpleForProduct(productId);
        return ResponseEntity.ok(new ResponseObject.Builder<List<VoucherSimpleResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Vouchers for product retrieved successfully")
                .build());
    }

    @GetMapping("{code}/applicable-products")
    public ResponseEntity<ResponseObject<List<ProductSimpleResponse>>> getApplicableProducts(
            @PathVariable String code) {
        var result = productAwareVoucherService.getApplicableProductsSimple(code);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductSimpleResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Applicable products retrieved successfully")
                .build());
    }
}
