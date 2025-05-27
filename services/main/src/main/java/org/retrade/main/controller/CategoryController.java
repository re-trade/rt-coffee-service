package org.retrade.main.controller;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.main.model.dto.response.CategoryResponse;
import org.retrade.main.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllVisibleCategories();
        return ResponseEntity.ok(new ResponseObject.Builder<List<CategoryResponse>>()
                .success(true)
                .code("CATEGORIES_RETRIEVED")
                .messages("Categories retrieved successfully")
                .content(categories)
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
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> getCategoriesByParent(@PathVariable String parentId) {
        List<CategoryResponse> categories = categoryService.getCategoriesByParent(parentId);
        return ResponseEntity.ok(new ResponseObject.Builder<List<CategoryResponse>>()
                .success(true)
                .code("CATEGORIES_RETRIEVED")
                .messages("Child categories retrieved successfully")
                .content(categories)
                .build());
    }

    @GetMapping("root")
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> getRootCategories() {
        List<CategoryResponse> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(new ResponseObject.Builder<List<CategoryResponse>>()
                .success(true)
                .code("CATEGORIES_RETRIEVED")
                .messages("Root categories retrieved successfully")
                .content(categories)
                .build());
    }

    @GetMapping("type/{type}")
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> getCategoriesByType(@PathVariable String type) {
        List<CategoryResponse> categories = categoryService.getCategoriesByType(type);
        return ResponseEntity.ok(new ResponseObject.Builder<List<CategoryResponse>>()
                .success(true)
                .code("CATEGORIES_RETRIEVED")
                .messages("Categories by type retrieved successfully")
                .content(categories)
                .build());
    }

    @GetMapping("search")
    public ResponseEntity<ResponseObject<List<CategoryResponse>>> searchCategories(@RequestParam String name) {
        List<CategoryResponse> categories = categoryService.searchCategoriesByName(name);
        return ResponseEntity.ok(new ResponseObject.Builder<List<CategoryResponse>>()
                .success(true)
                .code("CATEGORIES_RETRIEVED")
                .messages("Categories found successfully")
                .content(categories)
                .build());
    }
}
