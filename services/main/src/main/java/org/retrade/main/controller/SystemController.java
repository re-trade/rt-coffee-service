package org.retrade.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.UpdatePasswordRequest;
import org.retrade.main.service.AccountService;
import org.retrade.main.service.SellerService;
import org.retrade.main.service.SystemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("system")
@RestController
@RequiredArgsConstructor
public class SystemController {
    private final SystemService systemService;


    @PutMapping("/{id}/approve-seller")
    public ResponseEntity<ResponseObject<Void>> approveSeller(@PathVariable String id) {
        systemService.approveSeller(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Seller approved successfully")
                .build());
    }

    @PutMapping("/{id}/approve-product")
    public ResponseEntity<ResponseObject<Void>> approveProduct(@PathVariable String id) {
        systemService.approveProduct(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Product approved successfully")
                .build());
    }
}
