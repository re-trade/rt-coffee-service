package org.retrade.achievement.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.retrade.achievement.model.dto.response.SellerAchievementResponse;
import org.retrade.achievement.service.SellerAchievementService;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/seller-achievements")
public class SellerAchievementController {
    private final SellerAchievementService sellerAchievementService;

    @GetMapping("seller/{sellerId}/completed")
    public ResponseEntity<ResponseObject<List<SellerAchievementResponse>>> getSellerAchievementCompletedBySellerId (
            @PathVariable String sellerId,
            @PageableDefault Pageable pageable,
            @RequestParam(name = "q", required = false) String query
    ) {
        var queryWrapper = QueryWrapper.builder()
                .wrapSort(pageable)
                .search(query)
                .build();
        var result = sellerAchievementService.getSellerAchievementCompletedBySellerId(sellerId, queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<SellerAchievementResponse>>()
                .success(true)
                .code("SELLER_ACHIEVEMENT_RETRIEVED")
                .messages("Seller achievement retrieved successfully")
                .unwrapPaginationWrapper(result)
                .build());
    }

    @GetMapping("seller/me/completed")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<List<SellerAchievementResponse>>> getSellerAchievementCompleted (
            @PageableDefault Pageable pageable,
            @RequestParam(name = "q", required = false) String query,
            HttpServletRequest httpServletRequest
    ) {
        var queryWrapper = QueryWrapper.builder()
                .wrapSort(pageable)
                .search(query)
                .build();
        var result = sellerAchievementService.getSellerAchievementCompleted(httpServletRequest, queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<SellerAchievementResponse>>()
                .success(true)
                .code("SELLER_ACHIEVEMENT_RETRIEVED")
                .messages("Seller achievement retrieved successfully")
                .unwrapPaginationWrapper(result)
                .build());
    }
}
