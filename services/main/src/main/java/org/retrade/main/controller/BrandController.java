package org.retrade.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.BrandRequest;
import org.retrade.main.model.dto.response.BrandResponse;
import org.retrade.main.service.BrandService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brands")
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<ResponseObject<List<BrandResponse>>> getBrands(
            @RequestParam(required = false, name = "q") String query,
            @PageableDefault Pageable pageable) {
        var wrapper = QueryWrapper.builder().wrapSort(pageable).search(query).build();
        var response = brandService.getAllBrands(wrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<BrandResponse>>()
                .success(true)
                .code("BRANDS_RETRIEVED")
                .messages("Brands retrieved successfully")
                .unwrapPaginationWrapper(response)
                .build());
    }

    @PostMapping
    public ResponseEntity<ResponseObject<BrandResponse>> createBrand(@Valid @RequestBody BrandRequest request) {
        var response = brandService.createBrand(request);
        return ResponseEntity.ok(new ResponseObject.Builder<BrandResponse>()
                .success(true)
                .code("BRAND_SUBMIT")
                .messages("Brands retrieved successfully")
                .content(response)
                .build());
    }

    @PutMapping("{id}")
    public ResponseEntity<ResponseObject<BrandResponse>> updateBrand(@PathVariable String id, @Valid @RequestBody BrandRequest request) {
        var response = brandService.updateBrand(id, request);
        return ResponseEntity.ok(new ResponseObject.Builder<BrandResponse>()
                .success(true)
                .code("BRAND_SUBMIT")
                .messages("Brands retrieved successfully")
                .content(response)
                .build());
    }

    @GetMapping("all")
    public ResponseEntity<ResponseObject<List<BrandResponse>>> getAllBrandNoPaging() {
        var response = brandService.getAllBrandNoPaging();
        return ResponseEntity.ok(new ResponseObject.Builder<List<BrandResponse>>()
                .success(true)
                .code("BRAND_SUBMIT")
                .messages("Brands retrieved successfully")
                .content(response)
                .build());
    }
}

