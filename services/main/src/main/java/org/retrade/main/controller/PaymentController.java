package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.PaymentInitRequest;
import org.retrade.main.model.dto.response.*;
import org.retrade.main.service.PaymentService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "payments")
public class PaymentController {
    private final PaymentService paymentService;
    @Operation(summary = "Make Payment",
            description = "**(CUSTOMER ONLY)** This API make a payment, if pay by cash, it will do nothing, else will redirect to 3th service payment",
            security = {@SecurityRequirement(name = "accessCookie")}
    )
    @PostMapping("init")
    public ResponseEntity<ResponseObject<String>> paymentInit(
            @RequestBody PaymentInitRequest paymentInsertRequest,
            HttpServletRequest httpServletRequest) {

        return paymentService.initPayment(paymentInsertRequest, httpServletRequest)
                .map(url -> ResponseEntity.ok(new ResponseObject.Builder<String>()
                        .code("SUCCESS")
                        .messages("Đã tạo URL thanh toán thành công")
                        .success(true)
                        .content(url)
                        .build()))
                .orElseGet(() -> ResponseEntity.ok(new ResponseObject.Builder<String>()
                        .code("SUCCESS")
                        .messages("Không có URL thanh toán được tạo")
                        .success(true)
                        .content("")
                        .build()));
    }
    @Operation(summary = "PayOS Callback",
            description = "This API will for payos callback",
            security = {}
    )
    @GetMapping("callback/payos")
    public ResponseEntity<ResponseObject<Void>> paymentCallBack(HttpServletRequest request) {
        var result = paymentService.handlePaymentCallback(request, "PAY_OS");
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(result.getCallbackUrl())).body(new ResponseObject.Builder<Void>()
                .code(result.getSuccess() ? "SUCCESS" : "FAILURE")
                .messages(result.getMessage())
                .success(result.getSuccess())
                .build()
        );
    }
    @Operation(summary = "VNPay Callback",
            description = "This API will for vnpay callback",
            security = {}
    )
    @GetMapping("callback/vnp")
    public ResponseEntity<ResponseObject<Void>> paymentCallBackVN(HttpServletRequest request) {
        var result = paymentService.handlePaymentCallback(request, "VN_PAY");
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(result.getCallbackUrl())).body(new ResponseObject.Builder<Void>()
                .code(result.getSuccess() ? "SUCCESS" : "FAILURE")
                .messages(result.getMessage())
                .success(result.getSuccess())
                .build()
        );
    }
    @PostMapping("callback/vnp/ipn")
    public ResponseEntity<Map<String, String>> paymentVNPayIPN(HttpServletRequest request) {
        var response = new HashMap<String, String>();
        try {
            var result = paymentService.handleIPNWebhookCallback(request, "VN_PAY");
            if (result.getSuccess()) {
                response.put("RspCode", "00");
                response.put("Message", "Confirm Success");
            } else {
                response.put("RspCode", "97");
                response.put("Message", "Invalid signature");
            }
        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Unknow error");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("callback/payos/ipn")
    public ResponseEntity<Map<String, Boolean>> paymentPayOsIPN(HttpServletRequest request) {
        var response = new HashMap<String, Boolean>();
        try {
            var result = paymentService.handleIPNWebhookCallback(request, "PAY_OS");
            response.put("success", result.getSuccess());
        } catch (Exception e) {
            response.put("success", false);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("methods")
    public ResponseEntity<ResponseObject<List<PaymentMethodResponse>>> getPaymentMethods(@RequestParam(required = false, name = "q") String search, @PageableDefault Pageable pageable) {
        var query = QueryWrapper.builder()
                .search(search)
                .wrapSort(pageable)
                .build();
        var result = paymentService.getPaymentMethods(query);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject.Builder<List<PaymentMethodResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Lấy danh sách phương thức thanh toán thành công")
                .build());
    }

    @GetMapping("customer/{customerId}")
    public ResponseEntity<ResponseObject<List<ProductResponse>>> getPaymentHistoryByCustomerId(
            @PathVariable String customerId,
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {

        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = paymentService.getPaymentHistoriesByCustomerId(customerId, queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Lấy lịch sử thanh toán thành công")
                .build());
    }

    @GetMapping("order/{orderComboId}")
    public ResponseEntity<ResponseObject<PaymentOrderStatusResponse>> getPaymentOrderStatus(@PathVariable String orderComboId) {
        var result = paymentService.checkOrderPaymentStatusByOrderComboId(orderComboId);
        return ResponseEntity.ok(new ResponseObject.Builder<PaymentOrderStatusResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Kiểm tra trạng thái thanh toán thành công")
                .build());
    }

    @GetMapping("order/root/{orderId}")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<PaymentOrderBillStatusResponse>> checkOrderPaymentStatusByOrderId(@PathVariable String orderId) {
        var result = paymentService.checkOrderPaymentStatusByOrderId(orderId);
        return ResponseEntity.ok(new ResponseObject.Builder<PaymentOrderBillStatusResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Kiểm tra trạng thái thanh toán thành công")
                .build());
    }

    @GetMapping("order/root/{orderId}/history")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<List<PaymentHistoryResponse>>> getPaymentHistoryByOrderId(
            @PathVariable String orderId,
            @RequestParam(required = false, name = "q") String query,
            @PageableDefault Pageable pageable) {
        var result = paymentService.getOrderPaymentHistory(orderId, QueryWrapper.builder().search(query).wrapSort(pageable).build());
        return ResponseEntity.ok(new ResponseObject.Builder<List<PaymentHistoryResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Kiểm tra trạng thái thanh toán thành công")
                .build());
    }
}
