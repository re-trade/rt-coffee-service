package org.retrade.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.SellerRegisterRequest;
import org.retrade.main.model.dto.response.SellerRegisterResponse;
import org.retrade.main.service.FileService;
import org.retrade.main.service.SellerService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("sellers")
@RestController
@RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;
    private final FileService fileService;
    @PostMapping(path = "register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject<SellerRegisterResponse>> registerAsASeller(
            @Valid @ModelAttribute SellerRegisterRequest sellerRegisterRequest,
        @RequestPart MultipartFile backSideIdentityCard,
        @RequestPart MultipartFile frontSideIdentityCard
    ) {
        var front = fileService.uploadEncrypted(frontSideIdentityCard);
        var back = fileService.uploadEncrypted(backSideIdentityCard);
        var result = sellerService.createSeller(sellerRegisterRequest, front, back);
        return ResponseEntity.ok(new ResponseObject.Builder<SellerRegisterResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Register Seller Successfully")
                .build());
    }

}
