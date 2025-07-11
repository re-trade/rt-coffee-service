package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.CreateReportSellerRequest;
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
                .messages("Report seller created successfully")
                .build());
    }

    @GetMapping()
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
                .content(result)
                .messages("Get all report seller successfully")
                .build());
    }

    @GetMapping("{sellerId}")
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
                        .content(result)
                        .messages("Get all report by seller id successfully")
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
                .messages("Get all report seller successfully")
                .build());
    }

    @PatchMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<ReportSellerResponse>> acceptReportSeller(@PathVariable String id,@RequestParam boolean accept) {
        var result = reportSellerService.acceptReportSeller(id,accept);
        return ResponseEntity.ok(new ResponseObject.Builder<ReportSellerResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Accept report seller successfully")
                .build());
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<ReportSellerResponse>> processReportSeller(@PathVariable String id,@RequestParam String resolutionDetail) {
        var result = reportSellerService.processReportSeller(id, resolutionDetail);
        return ResponseEntity.ok(new ResponseObject.Builder<ReportSellerResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Accept report seller successfully")
                .build());
    }
}