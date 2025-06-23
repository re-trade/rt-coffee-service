package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.PaymentInitRequest;
import org.retrade.main.model.dto.response.PaymentMethodResponse;
import org.retrade.main.service.PaymentService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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
                        .messages("Payment URL generated successfully")
                        .success(true)
                        .content(url)
                        .build()))
                .orElseGet(() -> ResponseEntity.ok(new ResponseObject.Builder<String>()
                        .code("SUCCESS")
                        .messages("No payment URL generated")
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
                .messages("Methods retrieved successfully")
                .build());
    }

}
