package org.retrade.main.service;

import org.retrade.main.model.dto.response.CategoryResponse;
import org.retrade.main.model.entity.CategoryEntity;

import java.util.List;
import java.util.Set;

public interface CategoryService {
    CategoryResponse getCategoryById(String id);
    
    CategoryResponse getCategoryByName(String name);
    
    List<CategoryResponse> getAllVisibleCategories();
    
    List<CategoryResponse> getCategoriesByParent(String parentId);
    
    List<CategoryResponse> getRootCategories();
    
    List<CategoryResponse> getCategoriesByType(String type);
    
    List<CategoryResponse> searchCategoriesByName(String name);
    
    CategoryEntity getCategoryEntityById(String id);
    
    CategoryEntity getCategoryEntityByName(String name);
    
    boolean validateCategoryNames(Set<String> categoryNames);
    
    List<String> getInvalidCategoryNames(Set<String> categoryNames);
    
    boolean categoryExists(String name);
}
