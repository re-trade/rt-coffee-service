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
import org.retrade.main.model.dto.request.UpdateProductQuantityRequest;
import org.retrade.main.model.dto.request.UpdateProductRequest;
import org.retrade.main.model.dto.request.UpdateProductStatusRequest;
import org.retrade.main.model.dto.response.*;
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
                .messages("Tạo sản phẩm thành công")
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
                .messages("Cập nhật sản phẩm thành công")
                .build());
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<Void>> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Xóa sản phẩm thành công")
                .build());
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseObject<ProductResponse>> getProductById(@PathVariable String id) {
        var result = productService.getProductById(id);
        return ResponseEntity.ok(new ResponseObject.Builder<ProductResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("PLấy thông tin sản phẩm thành công")
                .build());
    }

    @GetMapping("{id}/price-histories")
    public ResponseEntity<ResponseObject<List<ProductPriceHistoryResponse>>> getProductPriceHistoryById(@PathVariable String id) {
        var result = productPriceHistoryService.getProductPriceHistoryList(id);
        return ResponseEntity.ok(new ResponseObject.Builder<List<ProductPriceHistoryResponse>>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("ấy danh sách sản phẩm thành công")
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
                .messages("Lấy danh sách sản phẩm thành công")
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
                .messages("Lấy danh sách sản phẩm thành công")
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
                .messages("Lấy danh sách sản phẩm thành công")
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
                .messages("ìm thấy sản phẩm thành công")
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
                .messages("ìm thấy sản phẩm thành công")
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
                .messages("Lọc sản phẩm thành công")
                .build());
    }

    @GetMapping("seller/filter")
    @PreAuthorize("hasRole('ROLE_SELLER')")
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
                .messages("Lọc sản phẩm thành công")
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
                .messages("Lấy danh sách sản phẩm theo danh mục thành công")
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
                .messages("Lấy danh sách sản phẩm theo danh mục thành công")
                .build());
    }

    @PutMapping("{id}/verify")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> verifyProduct(@PathVariable String id) {
        productService.verifyProduct(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Duyệt sản phẩm thành công")
                .build());
    }

    @PutMapping("{id}/unverify")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<Void>> unverifyProduct(@PathVariable String id) {
        productService.unverifyProduct(id);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Hủy duyệt sản phẩm thành công")
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
                .messages("Lấy thông tin sản phẩm thành công")
                .build());
    }
    @GetMapping("home-stats")
    public ResponseEntity<ResponseObject<ProductHomeStatsResponse>> homeStats(){
        var result = productService.getStatsHome();
        return ResponseEntity.ok(new ResponseObject.Builder<ProductHomeStatsResponse>()
                .success(true)
                .code("SUCCESS")
                .content(result)
                .messages("Lấy thống kê trang chủ thành công")
                .build());

    }

    @PatchMapping("status")
    public ResponseEntity<ResponseObject<Void>> updateProductStatus(@RequestBody UpdateProductStatusRequest request){
        productService.updateSellerProductStatus(request);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Cập nhật trạng thái sản phẩm thành công")
                .build());

    }

    @PatchMapping("quantity")
    public ResponseEntity<ResponseObject<Void>> updateProductQuantity(@RequestBody UpdateProductQuantityRequest request){
        productService.updateProductQuantity(request);
        return ResponseEntity.ok(new ResponseObject.Builder<Void>()
                .success(true)
                .code("SUCCESS")
                .messages("Cập nhật số lượng sản phẩm thành công")
                .build());
    }

    @GetMapping("id/random")
    public ResponseEntity<ResponseObject<RandomProductIdResponse>> randomProductId(){
        var random = productService.getRandomProductId();
        return ResponseEntity.ok(new ResponseObject.Builder<RandomProductIdResponse>()
                .success(true)
                .code("SUCCESS")
                .content(random)
                .messages("Lấy sản phẩm thành công")
                .build());
    }

}
