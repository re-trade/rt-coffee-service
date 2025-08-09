package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.CreateProductRequest;
import org.retrade.main.model.dto.request.UpdateProductRequest;
import org.retrade.main.model.dto.response.FieldAdvanceSearch;
import org.retrade.main.model.dto.response.ProductHomeStatsResponse;
import org.retrade.main.model.dto.response.ProductPriceHistoryResponse;
import org.retrade.main.model.dto.response.ProductResponse;
import org.retrade.main.service.ProductPriceHistoryService;
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
@Tag(name = "Products", description = "Product management and catalog endpoints")
public class ProductController {
    private final ProductService productService;
    private final ProductPriceHistoryService productPriceHistoryService;
    @Operation(
            summary = "Create new product",
            description = "Create a new product in the catalog. Requires SELLER role.",
            security = {@SecurityRequirement(name = "bearerAuth"), @SecurityRequirement(name = "cookieAuth")}
    )
    @PostMapping
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<ProductResponse>> createProduct(
            @Parameter(description = "Product creation data") @Valid @RequestBody CreateProductRequest request) {
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

    @GetMapping("{id}/price-histories")
    public ResponseEntity<ResponseObject<List<ProductPriceHistoryResponse>>> getProductPriceHistoryById(@PathVariable String id) {
        var result = productPriceHistoryService.getProductPriceHistoryList(id);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductPriceHistoryResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Product retrieved successfully")
                .build());
    }

    @Operation(
            summary = "Get all products",
            description = "Retrieve all products with optional search and pagination. Public endpoint."
    )
    @GetMapping
    public ResponseEntity<ResponseObject<List<ProductResponse>>> getAllProducts(
            @Parameter(description = "Search query to filter products") @RequestParam(required = false, name = "q") String search,
            @Parameter(description = "Pagination parameters") @PageableDefault(size = 10) Pageable pageable) {
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

    @GetMapping("search")
    public ResponseEntity<ResponseObject<List<ProductResponse>>> searchProductsByName(
            @PageableDefault Pageable pageable, @RequestParam(required = false, name = "q") String search
    ) {
        var queryWrapper = QueryWrapper.builder()
                .search(search)
                .pageable(pageable)
                .build();
        var result = productService.searchProductByKeyword(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Products found successfully")
                .build());
    }

    @GetMapping("similar")
    public ResponseEntity<ResponseObject<List<ProductResponse>>> searchProductSimilar(
            @PageableDefault Pageable pageable, @RequestParam(required = false, name = "q") String search
    ) {
        var queryWrapper = QueryWrapper.builder()
                .search(search)
                .pageable(pageable)
                .build();
        var result = productService.getProductSimilar(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Products found successfully")
                .build());
    }

    @GetMapping("filter")
    public ResponseEntity<ResponseObject<FieldAdvanceSearch>> getProductFilter(
            @RequestParam(required = false, name = "q") String search
    ) {
        var queryWrapper = QueryWrapper.builder()
                .search(search)
                .build();
        var result = productService.filedAdvanceSearch(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<FieldAdvanceSearch>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Filter product found successfully")
                .build());
    }

    @GetMapping("seller/filter")
    public ResponseEntity<ResponseObject<FieldAdvanceSearch>> getSellerProductFilter(
            @RequestParam(required = false, name = "q") String search
    ) {
        var queryWrapper = QueryWrapper.builder()
                .search(search)
                .build();
        var result = productService.sellerFiledAdvanceSearch(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<FieldAdvanceSearch>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Filter product found successfully")
                .build());
    }

    @GetMapping("category/{categoryName}")
    public ResponseEntity<ResponseObject<List<ProductResponse>>> getProductsByCategory(
            @PathVariable String categoryName,
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 10) Pageable pageable) {

        var queryWrapper = new QueryWrapper.QueryWrapperBuilder()
                .search(search)
                .wrapSort(pageable)
                .build();

        var result = productService.getProductsByCategory(categoryName, queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Products in category retrieved successfully")
                .build());
    }

    @GetMapping("category/{categoryName}/simple")
    public ResponseEntity<ResponseObject<List<ProductResponse>>> getProductsByCategorySimple(
            @PathVariable String categoryName) {
        var result = productService.getProductsByCategory(categoryName);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Products in category retrieved successfully")
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

    @GetMapping("best-selling")
    public ResponseEntity<ResponseObject<List<ProductResponse>>> searchProductBestSelling(
            @PageableDefault Pageable pageable, @RequestParam(required = false, name = "q") String search
    ) {
        var queryWrapper = QueryWrapper.builder()
                .search(search)
                .pageable(pageable)
                .build();
        var result = productService.searchProductBestSelling(queryWrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductResponse>>()
                .success(true)
                .code("SUCCESS")
                .unwrapPaginationWrapper(result)
                .messages("Products found successfully")
                .build());
    }
    @GetMapping("home-stats")
    public ResponseEntity<ResponseObject<ProductHomeStatsResponse>> homeStats(){
        var result = productService.getStatsHome();
        return ResponseEntity.ok(new ResponseObject.Builder<ProductHomeStatsResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("fetch stats home successfully")
                .build());

    }
}
