package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.request.CategoryRequest;
import org.retrade.main.model.dto.response.CategoryResponse;
import org.retrade.main.service.CategoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Product category management endpoints")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        var category = categoryService.createCategory(request);
        return ResponseEntity.ok(new ResponseObject.Builder<CategoryResponse>()
                .success(true)
                .code("CATEGORIES_RETRIEVED")
                .messages("Categories retrieved successfully")
                .content(category)
                .build());
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<CategoryResponse>> createCategory(@PathVariable String id,
            @Valid @RequestBody CategoryRequest request) {
        var category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(new ResponseObject.Builder<CategoryResponse>()
                .success(true)
                .code("CATEGORIES_RETRIEVED")
                .messages("Categories retrieved successfully")
                .content(category)
                .build());
    }

    @Operation(
            summary = "Get all categories",
            description = "Retrieve all visible categories with optional search and pagination. " +
                    "This endpoint returns categories that are marked as visible in the system."
    )
    @GetMapping
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> getAllCategories(
            @Parameter(description = "Pagination parameters (page, size, sort)") @PageableDefault Pageable pageable,
            @Parameter(description = "Search query to filter categories by name") @RequestParam(required = false, name = "q") String query
    ) {
        var wrapper = QueryWrapper.builder()
                .pageable(pageable)
                .search(query)
                .build();
        var categories = categoryService.getAllVisibleCategories(wrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<CategoryResponse>>()
                .success(true)
                .code("CATEGORIES_RETRIEVED")
                .messages("Categories retrieved successfully")
                .unwrapPaginationWrapper(categories)
                .build());
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseObject<CategoryResponse>> getCategoryById(@PathVariable String id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(new ResponseObject.Builder<CategoryResponse>()
                .success(true)
                .code("CATEGORY_RETRIEVED")
                .messages("Category retrieved successfully")
                .content(category)
                .build());
    }

    @GetMapping("by-name/{name}")
    public ResponseEntity<ResponseObject<CategoryResponse>> getCategoryByName(@PathVariable String name) {
        CategoryResponse category = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(new ResponseObject.Builder<CategoryResponse>()
                .success(true)
                .code("CATEGORY_RETRIEVED")
                .messages("Category retrieved successfully")
                .content(category)
                .build());
    }

    @Operation(
            summary = "Get categories by parent",
            description = "Retrieve child categories for a specific parent category with optional search and pagination."
    )
    @GetMapping("parent/{parentId}")
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> getCategoriesByParent(
            @Parameter(description = "ID of the parent category") @PathVariable String parentId,
            @Parameter(description = "Pagination parameters") @PageableDefault Pageable pageable,
            @Parameter(description = "Search query to filter child categories") @RequestParam(required = false, name = "q") String query ) {
        var wrapper = QueryWrapper.builder()
                .pageable(pageable)
                .search(query)
                .build();
        var categories = categoryService.getCategoriesByParent(parentId, wrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<CategoryResponse>>()
                .success(true)
                .code("CATEGORIES_RETRIEVED")
                .messages("Child categories retrieved successfully")
                .unwrapPaginationWrapper(categories)
                .build());
    }

    @Operation(
            summary = "Get root categories",
            description = "Retrieve top-level categories (categories without parent) with optional search and pagination."
    )
    @GetMapping("root")
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> getRootCategories(
            @Parameter(description = "Pagination parameters") @PageableDefault Pageable pageable,
            @Parameter(description = "Search query to filter root categories") @RequestParam(required = false, name = "q") String query
    ) {
        var wrapper = QueryWrapper.builder()
                .pageable(pageable)
                .search(query)
                .build();
        var categories = categoryService.getRootCategories(wrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<CategoryResponse>>()
                .success(true)
                .code("CATEGORIES_RETRIEVED")
                .messages("Root categories retrieved successfully")
                .unwrapPaginationWrapper(categories)
                .build());
    }

    @Operation(
            summary = "Get categories by type",
            description = "Retrieve categories filtered by a specific type with optional search and pagination."
    )
    @GetMapping("type/{type}")
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> getCategoriesByType(
            @Parameter(description = "Category type to filter by") @PathVariable String type,
            @Parameter(description = "Pagination parameters") @PageableDefault Pageable pageable,
            @Parameter(description = "Search query to filter categories") @RequestParam(required = false, name = "q") String query) {
        var wrapper = QueryWrapper.builder()
                .pageable(pageable)
                .search(query)
                .build();
        var categories = categoryService.getCategoriesByType(type, wrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<CategoryResponse>>()
                .success(true)
                .code("CATEGORIES_RETRIEVED")
                .messages("Categories by type retrieved successfully")
                .unwrapPaginationWrapper(categories)
                .build());
    }

    @Operation(
            summary = "Search categories",
            description = "Search for categories using a query string with pagination support. " +
                    "This endpoint searches across all categories regardless of visibility status."
    )
    @GetMapping("search")
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> searchCategories(
            @Parameter(description = "Pagination parameters") @PageableDefault Pageable pageable,
            @Parameter(description = "Search query string") @RequestParam(required = false, name = "q") String query) {
        var wrapper = QueryWrapper.builder()
                .pageable(pageable)
                .search(query)
                .build();
        var categories = categoryService.getAllCategories(wrapper);
        return ResponseEntity.ok(new ResponseObject.Builder<List<CategoryResponse>>()
                .success(true)
                .code("CATEGORIES_RETRIEVED")
                .messages("Categories found successfully")
                .unwrapPaginationWrapper(categories)
                .build());
    }
}
