package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.constant.SenderRoleEnum;
import org.retrade.main.model.dto.request.CreateEvidenceRequest;
import org.retrade.main.model.dto.request.CreateReportSellerRequest;
import org.retrade.main.model.dto.request.ReportSellerProcessRequest;
import org.retrade.main.model.dto.response.ReportSellerEvidenceResponse;
import org.retrade.main.model.dto.response.ReportSellerResponse;
import org.retrade.main.service.ReportSellerService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("report-seller")
@RequiredArgsConstructor
@Tag(name = "Report ", description = "User report product has buy endpoints")
public class ReportSellerController {
    private final ReportSellerService reportSellerService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<ReportSellerResponse>> createReport(
            @Parameter(description = "Report seller creation data") @Valid @RequestBody CreateReportSellerRequest request) {
        var result = reportSellerService.createReport(request);
        return ResponseEntity.ok(new ResponseObject.Builder<ReportSellerResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Tạo báo cáo người bán thành công")
                .build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<List<ReportSellerResponse>>> getAllReportSeller(
            @Parameter(description = "Search query to filter reports") @RequestParam(required = false, name = "q") String search,
            @Parameter(description = "Pagination parameters") @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();
        var result = reportSellerService.getAllReportSeller(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ReportSellerResponse>>()
                .success(true)
                .code("SUCCESS")
                        .unwrapPaginationWrapper(result)
                .messages("Lấy danh sách báo cáo thành công")
                .build());
    }

    @GetMapping("{id}/evidences/{type}" )
    public ResponseEntity<ResponseObject<List<ReportSellerEvidenceResponse>>> getAllReportSellerEvidence(
            @Parameter(description = "Search query to filter reports") @RequestParam(required = false, name = "q") String search,
            @Parameter(description = "Pagination parameters") @PageableDefault(size = 10) Pageable pageable,
            @Parameter(description = "Report Id") @PathVariable String id,
            @Parameter(description = "Request Type") @PathVariable SenderRoleEnum type
            ) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();
        var result = reportSellerService.getReportSellerEvidenceByReportId(id, type ,queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ReportSellerEvidenceResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Lấy danh sách báo cáo thành công")
                .build());
    }

    @GetMapping("seller/{sellerId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<List<ReportSellerResponse>>> getAllReportBySellerId(
            @PathVariable String sellerId,
            @Parameter(description = "Search query to filter reports")
            @RequestParam(required = false, name = "q") String search,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10) Pageable pageable) {

        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = reportSellerService.getAllReportBySellerId(sellerId, queryWrapper);

        return ResponseEntity.ok(
                new ResponseObject.Builder<List<ReportSellerResponse>>()
                        .success(true)
                        .code("SUCCESS")
                        .unwrapPaginationWrapper(result)
                        .messages("Lấy danh sách báo cáo theo người bán thành công")
                        .build()
        );
    }

    @GetMapping("seller/me")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<List<ReportSellerResponse>>> getAllReportBySellerMe(
            @Parameter(description = "Search query to filter reports")
            @RequestParam(required = false, name = "q") String search,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();
        var result = reportSellerService.getAllReportBySellerAuth(queryWrapper);
        return ResponseEntity.ok(
                new ResponseObject.Builder<List<ReportSellerResponse>>()
                        .success(true)
                        .code("SUCCESS")
                        .unwrapPaginationWrapper(result)
                        .messages("Lấy danh sách báo cáo của tôi (người bán) thành công.")
                        .build()
        );
    }

    @GetMapping("customer/me")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<List<ReportSellerResponse>>> getAllReportByCustomerMe(
            @Parameter(description = "Search query to filter reports")
            @RequestParam(required = false, name = "q") String search,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();
        var result = reportSellerService.getAllReportByCustomerAuth(queryWrapper);
        return ResponseEntity.ok(
                new ResponseObject.Builder<List<ReportSellerResponse>>()
                        .success(true)
                        .code("SUCCESS")
                        .unwrapPaginationWrapper(result)
                        .messages("Lấy danh sách báo cáo của tôi (người mua) thành công.")
                        .build()
        );
    }


    @GetMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<ReportSellerResponse>> getReportDetail(@PathVariable String id) {
        var result = reportSellerService.getReportDetail(id);
        return ResponseEntity.ok(new ResponseObject.Builder<ReportSellerResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Lấy thông tin báo cáo thành công")
                .build());
    }

    @GetMapping("{id}/customer")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<ReportSellerResponse>> getCustomerReportDetail(@PathVariable String id) {
        var result = reportSellerService.getReportDetail(id);
        return ResponseEntity.ok(new ResponseObject.Builder<ReportSellerResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Lấy thông tin báo cáo thành công")
                .build());
    }

    @PatchMapping("{id}/accept")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<ReportSellerResponse>> acceptReportSeller(@PathVariable("id") String reportId) {
        var result = reportSellerService.acceptReport(reportId);
        return ResponseEntity.ok(new ResponseObject.Builder<ReportSellerResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Chấp nhận báo cáo thành công")
                .build());
    }

    @PatchMapping("{id}/reject")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<ReportSellerResponse>> rejectReportSeller(@PathVariable("id") String reportId) {
        var result = reportSellerService.rejectReport(reportId);
        return ResponseEntity.ok(new ResponseObject.Builder<ReportSellerResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Từ chối báo cáo thành công")
                .build());
    }


    @PutMapping("{id}/process")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<ReportSellerResponse>> processReportSeller(@PathVariable String id, @RequestBody ReportSellerProcessRequest request) {
        var result = reportSellerService.processReportSeller(id, request);
        return ResponseEntity.ok(new ResponseObject.Builder<ReportSellerResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Chấp nhận báo cáo thành công")
                .build());
    }

    @PostMapping("{id}/evidences/seller")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<ReportSellerEvidenceResponse>> sellerUploadEvidence(@PathVariable String id, @RequestBody CreateEvidenceRequest createEvidenceRequest) {
        var result = reportSellerService.addSellerEvidence(id, createEvidenceRequest);
        return ResponseEntity.ok(new ResponseObject.Builder<ReportSellerEvidenceResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Gửi báo cáo thành công")
                .build());
    }

    @PostMapping("{id}/evidences/customer")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseObject<ReportSellerEvidenceResponse>> customerUploadEvidence(@PathVariable String id, @RequestBody CreateEvidenceRequest createEvidenceRequest) {
        var result = reportSellerService.addCustomerEvidence(id, createEvidenceRequest);
        return ResponseEntity.ok(new ResponseObject.Builder<ReportSellerEvidenceResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Gửi báo cáo thành công")
                .build());
    }

    @PostMapping("{id}/evidences/system")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<ReportSellerEvidenceResponse>> adminUploadEvidence(@PathVariable String id, @RequestBody CreateEvidenceRequest createEvidenceRequest) {
        var result = reportSellerService.addSystemEvidence(id, createEvidenceRequest);
        return ResponseEntity.ok(new ResponseObject.Builder<ReportSellerEvidenceResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Gửi báo cáo thành công")
                .build());
    }
}