package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.response.ProductHistoryResponse;
import org.retrade.main.service.ProductHistoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("product-histories")
@RequiredArgsConstructor
public class ProductHistoryController {
    private final ProductHistoryService productHistoryService;
    @GetMapping("product/{productId}")
    public ResponseEntity<ResponseObject<List<ProductHistoryResponse>>> getProductHistoriesByProductId
            (@PathVariable String productId,
             @RequestParam(name = "q", required = false) String query,
            @PageableDefault Pageable page
            ) {
        var queryWrapper = QueryWrapper.builder()
                .search(query)
                .wrapSort(page)
                .build();
        var response = productHistoryService.getProductHistoryByProductId(productId, queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductHistoryResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(response)
                .messages("Product history return successfully")
                .build());
    }
}
