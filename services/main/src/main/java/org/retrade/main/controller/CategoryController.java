package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.response.CategoryResponse;
import org.retrade.main.service.CategoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> getAllCategories(
            @PageableDefault Pageable pageable,
            @RequestParam(required = false, name = "q") String query
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

    @GetMapping("parent/{parentId}")
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> getCategoriesByParent(@PathVariable String parentId,
                                                                                        @PageableDefault Pageable pageable,
                                                                                        @RequestParam(required = false, name = "q") String query ) {
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

    @GetMapping("root")
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> getRootCategories(
            @PageableDefault Pageable pageable,
            @RequestParam(required = false, name = "q") String query
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

    @GetMapping("type/{type}")
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> getCategoriesByType(
            @PathVariable String type,
            @PageableDefault Pageable pageable,
            @RequestParam(required = false, name = "q") String query) {
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

    @GetMapping("search")
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> searchCategories(
            @PageableDefault Pageable pageable,
            @RequestParam(required = false, name = "q") String query) {
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
