package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.response.RevenueResponse;
import org.retrade.main.model.dto.response.RevenueStatResponse;
import org.retrade.main.model.dto.response.ReviewStatsResponse;
import org.retrade.main.service.RevenueService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("revenue")
public class RevenueController {

    private final RevenueService revenueService;
    @GetMapping("my-revenue")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<List<RevenueResponse>>> getMyProducts(
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {

        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = revenueService.getMyRevenue(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<RevenueResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Lấy doanh thu sản phẩm thành công")
                .build());
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("stats")
    public ResponseEntity<ResponseObject<RevenueStatResponse>> getStatsSeller(){
        var result = revenueService.getStatsRevenue();
        return ResponseEntity.ok(new ResponseObject.Builder<RevenueStatResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Lấy thống kê doanh thu người bán thành công")
                .build());

    }


}
