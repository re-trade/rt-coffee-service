package org.retrade.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.CreateProductRequest;
import org.retrade.main.model.dto.request.UpdateProductRequest;
import org.retrade.main.model.dto.response.ProductResponse;
import org.retrade.main.service.ProductService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        var result = productService.createProduct(request);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Product created successfully")
                .build());
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<ProductResponse>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request) {
        var result = productService.updateProduct(id, request);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Product updated successfully")
                .build());
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<Void>> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Product deleted successfully")
                .build());
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseObject<ProductResponse>> getProductById(@PathVariable String id) {
        var result = productService.getProductById(id);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Product retrieved successfully")
                .build());
    }

    @GetMapping
    public ResponseEntity<ResponseObject<List<ProductResponse>>> getAllProducts(
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {
        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = productService.getAllProducts(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Products retrieved successfully")
                .build());
    }

    @GetMapping("seller/{sellerId}")
    public ResponseEntity<ResponseObject<List<ProductResponse>>> getProductsBySeller(
            @PathVariable String sellerId,
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {

        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = productService.getProductsBySeller(sellerId, queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Products retrieved successfully")
                .build());
    }

    @GetMapping("my-products")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<List<ProductResponse>>> getMyProducts(
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {

        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = productService.getMyProducts(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Your products retrieved successfully")
                .build());
    }

    @GetMapping("brand/{brand}")
    public ResponseEntity<ResponseObject<List<ProductResponse>>> getProductsByBrand(
            @PathVariable String brand) {
        var result = productService.getProductsByBrand(brand);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Products retrieved successfully")
                .build());
    }

    @GetMapping("search")
    public ResponseEntity<ResponseObject<List<ProductResponse>>> searchProductsByName(
            @RequestParam String name) {
        var result = productService.searchProductsByName(name);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Products found successfully")
                .build());
    }

    @PutMapping("{id}/verify")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> verifyProduct(@PathVariable String id) {
        productService.verifyProduct(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Product verified successfully")
                .build());
    }

    @PutMapping("{id}/unverify")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> unverifyProduct(@PathVariable String id) {
        productService.unverifyProduct(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Product unverified successfully")
                .build());
    }
}
