package org.retrade.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.ApproveSellerRequest;
import org.retrade.main.model.dto.request.SellerRegisterRequest;
import org.retrade.main.model.dto.request.SellerUpdateRequest;
import org.retrade.main.model.dto.response.SellerBaseResponse;
import org.retrade.main.model.dto.response.SellerRegisterResponse;
import org.retrade.main.service.FileService;
import org.retrade.main.service.SellerService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("sellers")
@RestController
@RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;
    private final FileService fileService;
    @PostMapping(path = "register")
    public ResponseEntity<ResponseObject<SellerRegisterResponse>> registerAsASeller(
            @Valid @RequestBody SellerRegisterRequest sellerRegisterRequest
    ) {
        var result = sellerService.createSeller(sellerRegisterRequest);
        return ResponseEntity.ok(new ResponseObject.Builder<SellerRegisterResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Register Seller Successfully")
                .build());
    }

    @PutMapping(path = "identity/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject<SellerRegisterResponse>> registerAsASeller(
            @RequestPart MultipartFile backSideIdentityCard,
            @RequestPart MultipartFile frontSideIdentityCard
    ) {
        var front = fileService.uploadEncrypted(frontSideIdentityCard);
        var back = fileService.uploadEncrypted(backSideIdentityCard);
        var result = sellerService.cccdSubmit(front, back);
        return ResponseEntity.ok(new ResponseObject.Builder<SellerRegisterResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("CCCD Seller Submit Successfully")
                .build());
    }

    @PutMapping(path = "profile")
    public ResponseEntity<ResponseObject<SellerBaseResponse>> updateSellerProfile(@RequestBody @Valid SellerUpdateRequest request) {
        var result = sellerService.updateSellerProfile(request);
        return ResponseEntity.ok(new ResponseObject.Builder<SellerBaseResponse>()
                .success(true)
                .code("SUCCESS")
                .messages("Profile Seller Save Successfully")
                .content(result)
                .build());
    }

    @PatchMapping(path = "approve")
    public ResponseEntity<ResponseObject<Void>> approveSeller(
            @Valid @RequestBody ApproveSellerRequest request) {
        sellerService.approveSeller(request);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Approve Seller Submit Successfully")
                .build());
    }
    @GetMapping("{id}")
    public ResponseEntity<ResponseObject<SellerBaseResponse>> getSellerById(@PathVariable String id) {

        var result = sellerService.getSellerDetails(id);
        return  ResponseEntity.ok(new ResponseObject.Builder<SellerBaseResponse>()
                .success(true)
                .code("SUCCESS")
                .messages("Profile Seller get Successfully")
                .content(result)
                .build());
    }

    @GetMapping
    public ResponseEntity<ResponseObject<SellerBaseResponse>> getMySellers() {

        var result = sellerService.getMySellers();
        return  ResponseEntity.ok(new ResponseObject.Builder<SellerBaseResponse>()
                .success(true)
                .code("SUCCESS")
                .messages("Profile Seller get Successfully")
                .content(result)
                .build());
    }

    @PutMapping("{id}/ban-seller")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> banSeller(@PathVariable String id) {
        sellerService.banSeller(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("ban seller with" + id + "successfully")
                .build());
    }

    @PutMapping("{id}/unban-seller")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> unbanSeller(@PathVariable String id) {
        sellerService.banSeller(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("ban seller with" + id + "successfully")
                .build());
    }
}
