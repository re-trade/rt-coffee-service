package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.PlatformFeeTierInsertRequest;
import org.retrade.main.model.dto.response.PlatformFeeTierResponse;
import org.retrade.main.service.PlatformSettingService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("platform-settings")
public class PlatformSettingController {
    private final PlatformSettingService platformSettingService;

    @GetMapping("fee")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<List<PlatformFeeTierResponse>>> getPlatformFeeTierConfig(@PageableDefault Pageable pageable, @RequestParam(required = false) String q) {
        var result = platformSettingService.getAllPlatformFeeTierConfig(QueryWrapper.builder()
                        .wrapSort(pageable)
                        .search(q)
                .build());
        return ResponseEntity.ok(new ResponseObject.Builder<List<PlatformFeeTierResponse>>()
                .success(true)
                .code("SUCCESS")
                .messages("Get platform fee tier config successfully.")
                .unwrapPaginationWrapper(result)
                .build());
    }

    @PostMapping("fee")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<PlatformFeeTierResponse>> insertPlatformFeeTierConfig(@RequestBody PlatformFeeTierInsertRequest request) {
        var result = platformSettingService.upsertTier(request);
        return ResponseEntity.ok(new ResponseObject.Builder<PlatformFeeTierResponse>()
                .success(true)
                .code("SUCCESS")
                .messages("Insert platform fee tier config successfully.")
                .content(result)
                .build());
    }

    @PutMapping("fee/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<PlatformFeeTierResponse>> updatePlatformFeeTierConfig(@RequestBody PlatformFeeTierInsertRequest request, @PathVariable String id) {
        var result = platformSettingService.updateTier(id, request);
        return ResponseEntity.ok(new ResponseObject.Builder<PlatformFeeTierResponse>()
                .success(true)
                .code("SUCCESS")
                .messages("Update platform fee tier config successfully.")
                .content(result)
                .build());
    }
}
