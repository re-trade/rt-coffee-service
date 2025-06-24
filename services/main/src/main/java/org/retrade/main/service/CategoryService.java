package org.retrade.main.service;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.request.CategoryRequest;
import org.retrade.main.model.dto.response.CategoryResponse;
import org.retrade.main.model.entity.CategoryEntity;

import java.util.List;
import java.util.Set;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(String id, CategoryRequest request);

    CategoryResponse getCategoryById(String id);
    
    CategoryResponse getCategoryByName(String name);
    
    PaginationWrapper<List<CategoryResponse>> getAllVisibleCategories(QueryWrapper queryWrapper);

    PaginationWrapper<List<CategoryResponse>> getAllCategories(QueryWrapper queryWrapper);

    PaginationWrapper<List<CategoryResponse>> getCategoriesByParent(String parentId, QueryWrapper queryWrapper);
    
    PaginationWrapper<List<CategoryResponse>> getRootCategories(QueryWrapper queryWrapper);
    
    PaginationWrapper<List<CategoryResponse>> getCategoriesByType(String type, QueryWrapper queryWrapper);
    
    CategoryEntity getCategoryEntityById(String id);
    
    CategoryEntity getCategoryEntityByName(String name);
    
    boolean validateCategoryNames(Set<String> categoryNames);
    
    List<String> getInvalidCategoryNames(Set<String> categoryNames);
    
    boolean categoryExists(String name);

    List<CategoryResponse> getAllCategoriesNoPagination();
}
