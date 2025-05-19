package org.retrade.voucher.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.voucher.model.dto.request.CreateVoucherRequest;
import org.retrade.voucher.model.dto.request.UpdateVoucherRequest;
import org.retrade.voucher.model.dto.response.VoucherResponse;
import org.retrade.voucher.service.VoucherService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
public class VoucherAdminController {
    private final VoucherService voucherService;

    @PostMapping
    public ResponseEntity<ResponseObject<VoucherResponse>> createVoucher(@Valid @RequestBody CreateVoucherRequest request) {
        VoucherResponse response = voucherService.createVoucher(request);
        return ResponseEntity.ok(new ResponseObject.Builder<VoucherResponse>()
                .success(true)
                .code("VOUCHER_CREATED")
                .messages("Voucher created successfully")
                .content(response)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<VoucherResponse>> updateVoucher(
            @PathVariable String id,
            @Valid @RequestBody UpdateVoucherRequest request) {
        VoucherResponse response = voucherService.updateVoucher(id, request);
        return ResponseEntity.ok(new ResponseObject.Builder<VoucherResponse>()
                .success(true)
                .code("VOUCHER_UPDATED")
                .messages("Voucher updated successfully")
                .content(response)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteVoucher(@PathVariable String id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("VOUCHER_DELETED")
                .messages("Voucher deleted successfully")
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<VoucherResponse>> getVoucherById(@PathVariable String id) {
        VoucherResponse response = voucherService.getVoucherById(id);
        return ResponseEntity.ok(new ResponseObject.Builder<VoucherResponse>()
                .success(true)
                .code("VOUCHER_FOUND")
                .content(response)
                .build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseObject<VoucherResponse>> getVoucherByCode(@PathVariable String code) {
        VoucherResponse response = voucherService.getVoucherByCode(code);
        return ResponseEntity.ok(new ResponseObject.Builder<VoucherResponse>()
                .success(true)
                .code("VOUCHER_FOUND")
                .content(response)
                .build());
    }

    @GetMapping("/active")
    public ResponseEntity<ResponseObject<List<VoucherResponse>>> getActiveVouchers() {
        List<VoucherResponse> responses = voucherService.getActiveVouchers();
        return ResponseEntity.ok(new ResponseObject.Builder<List<VoucherResponse>>()
                .success(true)
                .code("ACTIVE_VOUCHERS_FOUND")
                .content(responses)
                .build());
    }

    @GetMapping
    public ResponseEntity<ResponseObject<List<VoucherResponse>>> getVouchers(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        QueryWrapper queryWrapper = QueryWrapper.builder()
                .search(query)
                .pageable(pageable)
                .build();
        
        PaginationWrapper<List<VoucherResponse>> paginationWrapper = voucherService.getVouchers(queryWrapper);
        
        return ResponseEntity.ok(new ResponseObject.Builder<List<VoucherResponse>>()
                .success(true)
                .code("VOUCHERS_FOUND")
                .unwrapPaginationWrapper(paginationWrapper)
                .build());
    }
}
