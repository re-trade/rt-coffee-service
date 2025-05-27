package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.response.CategoryResponse;
import org.retrade.main.model.entity.CategoryEntity;
import org.retrade.main.repository.CategoryRepository;
import org.retrade.main.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse getCategoryById(String id) {
        CategoryEntity category = getCategoryEntityById(id);
        return mapToCategoryResponse(category);
    }

    @Override
    public CategoryResponse getCategoryByName(String name) {
        CategoryEntity category = getCategoryEntityByName(name);
        return mapToCategoryResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllVisibleCategories() {
        List<CategoryEntity> categories = categoryRepository.findByVisible(true);
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getCategoriesByParent(String parentId) {
        CategoryEntity parent = getCategoryEntityById(parentId);
        List<CategoryEntity> categories = categoryRepository.findByCategoryParent(parent);
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getRootCategories() {
        List<CategoryEntity> categories = categoryRepository.findByCategoryParentIsNull();
        return categories.stream()
                .filter(CategoryEntity::getVisible)
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getCategoriesByType(String type) {
        List<CategoryEntity> categories = categoryRepository.findByVisibleAndType(true, type);
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> searchCategoriesByName(String name) {
        List<CategoryEntity> categories = categoryRepository.findByNameContainingIgnoreCase(name);
        return categories.stream()
                .filter(CategoryEntity::getVisible)
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryEntity getCategoryEntityById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Category not found with id: " + id));
    }

    @Override
    public CategoryEntity getCategoryEntityByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ValidationException("Category not found with name: " + name));
    }

    @Override
    public boolean validateCategoryNames(Set<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return true;
        }
        
        for (String categoryName : categoryNames) {
            if (!categoryExists(categoryName)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> getInvalidCategoryNames(Set<String> categoryNames) {
        List<String> invalidCategories = new ArrayList<>();
        
        if (categoryNames == null || categoryNames.isEmpty()) {
            return invalidCategories;
        }
        
        for (String categoryName : categoryNames) {
            if (!categoryExists(categoryName)) {
                invalidCategories.add(categoryName);
            }
        }
        return invalidCategories;
    }

    @Override
    public boolean categoryExists(String name) {
        return categoryRepository.existsByName(name);
    }

    private CategoryResponse mapToCategoryResponse(CategoryEntity category) {
        CategoryResponse.CategoryResponseBuilder builder = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .visible(category.getVisible())
                .type(category.getType())
                .createdAt(category.getCreatedDate().toLocalDateTime())
                .updatedAt(category.getUpdatedDate().toLocalDateTime());

        if (category.getCategoryParent() != null) {
            builder.parentId(category.getCategoryParent().getId())
                   .parentName(category.getCategoryParent().getName());
        }

        if (category.getSeller() != null) {
            builder.sellerId(category.getSeller().getId())
                   .sellerShopName(category.getSeller().getShopName());
        }

        List<CategoryEntity> children = categoryRepository.findByCategoryParent(category);
        if (!children.isEmpty()) {
            List<CategoryResponse> childrenResponses = children.stream()
                    .filter(CategoryEntity::getVisible)
                    .map(this::mapToCategoryResponse)
                    .collect(Collectors.toList());
            builder.children(childrenResponses);
        }

        return builder.build();
    }
}
