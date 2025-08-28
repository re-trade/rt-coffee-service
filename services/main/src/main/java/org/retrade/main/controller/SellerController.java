package org.retrade.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.constant.IdentityCardTypeEnum;
import org.retrade.main.model.dto.request.ApproveSellerRequest;
import org.retrade.main.model.dto.request.SellerRegisterRequest;
import org.retrade.main.model.dto.request.SellerUpdateRequest;
import org.retrade.main.model.dto.response.SellerBaseMetricResponse;
import org.retrade.main.model.dto.response.SellerRegisterResponse;
import org.retrade.main.model.dto.response.SellerResponse;
import org.retrade.main.model.dto.response.SellerStatusResponse;
import org.retrade.main.service.FileService;
import org.retrade.main.service.SellerService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
                .messages("Khởi tạo thông tin người bán thành công")
                .build());
    }

    @DeleteMapping(path = "profile")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<Void>> removeSellerProfileInit() {
        sellerService.removeSellerProfileInit();
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Xóa thông tin người bán thành công")
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
                .messages("Gửi thông tin căn cước công dân thành công")
                .build());
    }

    @GetMapping("profile")
    public ResponseEntity<ResponseObject<SellerResponse>> getMySellers() {
        var result = sellerService.getMySellers();
        return  ResponseEntity.ok(new ResponseObject.Builder<SellerResponse>()
                .success(true)
                .code("SUCCESS")
                .messages("Lấy hồ sơ người bán thành công")
                .content(result)
                .build());
    }

    @GetMapping()
    public ResponseEntity<ResponseObject<List<SellerResponse>>> getSellers(
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        var result = sellerService.getSellers(QueryWrapper.builder()
                        .wrapSort(pageable)
                        .search(search)
                .build());
        return ResponseEntity.ok(new ResponseObject.Builder<List<SellerResponse>>()
                        .unwrapPaginationWrapper(result)
                        .success(true)
                        .code("SUCCESS")
                        .messages("Lấy danh sách  người bán thành công")
                        .build());
    }

    @PutMapping(path = "profile")
    public ResponseEntity<ResponseObject<SellerResponse>> updateSellerProfile(@RequestBody @Valid SellerUpdateRequest request) {
        var result = sellerService.updateSellerProfile(request);
        return ResponseEntity.ok(new ResponseObject.Builder<SellerResponse>()
                .success(true)
                .code("SUCCESS")
                .messages("Cập nhật hồ sơ người bán thành công")
                .content(result)
                .build());
    }

    @PatchMapping(path = "approve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> approveSeller(
            @Valid @RequestBody ApproveSellerRequest request) {
        sellerService.approveSeller(request);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Phê duyệt yêu cầu người bán thành công")
                .build());
    }
    @GetMapping("{id}")
    public ResponseEntity<ResponseObject<SellerResponse>> getSellerById(@PathVariable String id) {

        var result = sellerService.getSellerDetails(id);
        return  ResponseEntity.ok(new ResponseObject.Builder<SellerResponse>()
                .success(true)
                .code("SUCCESS")
                .messages("Lấy hồ sơ người bán thành công")
                .content(result)
                .build());
    }



    @PutMapping("{id}/ban-seller")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<SellerResponse>> banSeller(@PathVariable String id) {
        sellerService.banSeller(id);
        return ResponseEntity.ok(new ResponseObject.Builder<SellerResponse>()
                .success(true)
                .code("SUCCESS")
                .messages("Cấm người bán với id" + id + "thành công")
                .build());
    }

    @PutMapping("{id}/unban-seller")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<SellerResponse>> unbanSeller(@PathVariable String id) {
        sellerService.unbanSeller(id);
        return ResponseEntity.ok(new ResponseObject.Builder<SellerResponse>()
                .success(true)
                .code("SUCCESS")
                .messages("Bỏ cấm người bán với id" + id + " thành công")
                .build());
    }

    @GetMapping("{id}/metric")
    public ResponseEntity<ResponseObject<SellerBaseMetricResponse>> getSellerStats (@PathVariable String id) {
        var metrics = sellerService.getSellerBaseMetric(id);
        return ResponseEntity.ok(new ResponseObject.Builder<SellerBaseMetricResponse>()
                .success(true)
                .code("SUCCESS")
                .content(metrics)
                .messages("Lấy thông kê người bán thành công")
                .build());
    }

    @GetMapping("{id}/id-card")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<byte[]> getSellerIdentityCard(@PathVariable String id, @RequestParam IdentityCardTypeEnum cardType) {
        var result = fileService.getSellerIdentityCard(id, cardType);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(result.mimeType()));
        return new ResponseEntity<>(result.bytes(), headers, HttpStatus.OK);
    }

    @GetMapping("status")
    public ResponseEntity<ResponseObject<SellerStatusResponse>> getSellerStatus () {
        var result = sellerService.checkSellerStatus();
        return ResponseEntity.ok(new ResponseObject.Builder<SellerStatusResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Lấy trạng thái người bán thành công")
                .build());
    }
}
