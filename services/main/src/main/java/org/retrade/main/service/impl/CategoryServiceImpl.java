package org.retrade.main.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.response.CategoryResponse;
import org.retrade.main.model.entity.CategoryEntity;
import org.retrade.main.repository.CategoryRepository;
import org.retrade.main.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.*;
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
    public PaginationWrapper<List<CategoryResponse>> getAllVisibleCategories(QueryWrapper queryWrapper) {
        return categoryRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("visible"), true));
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToCategoryResponse).stream().toList();
            return new PaginationWrapper.Builder<List<CategoryResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public PaginationWrapper<List<CategoryResponse>> getAllCategories(QueryWrapper queryWrapper) {
        return categoryRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToCategoryResponse).stream().toList();
            return new PaginationWrapper.Builder<List<CategoryResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });

    }

    @Override
    public PaginationWrapper<List<CategoryResponse>> getCategoriesByParent(String parentId, QueryWrapper queryWrapper) {
        return categoryRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            var parentJoin = root.join("categoryParent");
            predicates.add(criteriaBuilder.equal(parentJoin.get("id"), parentId));
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToCategoryResponse).stream().toList();
            return new PaginationWrapper.Builder<List<CategoryResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });

    }

    @Override
    public PaginationWrapper<List<CategoryResponse>> getRootCategories(QueryWrapper queryWrapper) {
        return categoryRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            var parentJoin = root.join("categoryParent", JoinType.LEFT);
            predicates.add(criteriaBuilder.isNull(parentJoin.get("id")));
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToCategoryResponse).stream().toList();
            return new PaginationWrapper.Builder<List<CategoryResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public PaginationWrapper<List<CategoryResponse>> getCategoriesByType(String type, QueryWrapper queryWrapper) {
        return categoryRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.like(root.get("type"), "%" + type + "%"));
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToCategoryResponse).stream().toList();
            return new PaginationWrapper.Builder<List<CategoryResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });

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
            if (categoryExists(categoryName)) {
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
            if (categoryExists(categoryName)) {
                invalidCategories.add(categoryName);
            }
        }
        return invalidCategories;
    }

    @Override
    public boolean categoryExists(String name) {
        return !categoryRepository.existsByName(name);
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

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<CategoryEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = categoryRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
