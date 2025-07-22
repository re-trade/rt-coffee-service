package org.retrade.main.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.constant.QueryOperatorEnum;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.request.BrandRequest;
import org.retrade.main.model.dto.response.BrandResponse;
import org.retrade.main.model.entity.BrandEntity;
import org.retrade.main.repository.jpa.BrandRepository;
import org.retrade.main.repository.jpa.CategoryRepository;
import org.retrade.main.service.BrandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        var brandEntity = BrandEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imgUrl(request.getImgUrl())
                .enabled(true)
                .build();
        var categories = categoryRepository.findAllById(request.getCategoryIds());
        if (!categories.isEmpty()) {
            brandEntity.setCategories(new HashSet<>(categories));
        }
        try {
            var response = brandRepository.save(brandEntity);
            return mapToBrandResponse(response);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to create brand", ex);
        }
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(String id, BrandRequest request) {
        var brand = brandRepository.findById(id).orElseThrow(() -> new ActionFailedException("Brand not found"));
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setImgUrl(request.getImgUrl());
        var categories = categoryRepository.findAllById(request.getCategoryIds());
        if (!categories.isEmpty()) {
            brand.setCategories(new HashSet<>(categories));
        }
        try {
            var response = brandRepository.save(brand);
            return mapToBrandResponse(response);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to update brand", ex);
        }
    }

    @Override
    public PaginationWrapper<List<BrandResponse>> getAllBrands(QueryWrapper queryWrapper) {
        return brandRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToBrandResponse).stream().toList();
            return new PaginationWrapper.Builder<List<BrandResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public PaginationWrapper<List<BrandResponse>> getAllBrandByCategoriesList(QueryWrapper queryWrapper) {
        var categories = queryWrapper.search().remove("categoryId");
        if (categories == null) {
            throw new ValidationException("categoryId is required for this query");
        }
        return brandRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            var categoryIds = new ArrayList<String>();
            if (categories.getOperator() == QueryOperatorEnum.IN && categories.getValue() instanceof List<?>) {
                var temp = ((List<?>) categories.getValue()).stream().map(String::valueOf).toList();
                categoryIds.addAll(temp);
            }else if (categories.getOperator() == QueryOperatorEnum.EQ && categories.getValue() instanceof String) {
                categoryIds.add((String) categories.getValue());
            }
            if (!categoryIds.isEmpty()) {
                var categoryJoin = root.join("categories", JoinType.INNER);
                predicates.add(categoryJoin.get("id").in(categoryIds));
            }
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToBrandResponse).stream().toList();
            return new PaginationWrapper.Builder<List<BrandResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public List<BrandResponse> getAllBrandNoPaging() {
        List<BrandEntity> brandEntities = brandRepository.findAll();
        return brandEntities.stream().map(this::mapToBrandResponse).collect(Collectors.toList());
    }

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<BrandEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = brandRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private BrandResponse mapToBrandResponse(BrandEntity brandEntity) {
        return BrandResponse.builder()
                .name(brandEntity.getName())
                .id(brandEntity.getId())
                .imgUrl(brandEntity.getImgUrl())
                .build();
    }
}
