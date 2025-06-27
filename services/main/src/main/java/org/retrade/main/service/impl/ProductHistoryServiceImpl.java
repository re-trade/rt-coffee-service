package org.retrade.main.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.main.model.dto.response.ProductHistoryResponse;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.repository.ProductRepository;
import org.retrade.main.service.ProductHistoryService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductHistoryServiceImpl implements ProductHistoryService {
    private final ProductRepository productRepository;

    @Override
    public PaginationWrapper<List<ProductHistoryResponse>> getProductHistoryByProductId(String productId, QueryWrapper queryWrapper) {
        Set<String> ancestryIds = productRepository.findProductAncestryIds(productId);

        return productRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            var builder = criteriaBuilder.in(root.get("id")).value(ancestryIds);
            ancestryIds.forEach(builder::value);
            predicates.add(builder);
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::wrapProductHistoryResponse).stream().toList();
            return new PaginationWrapper.Builder<List<ProductHistoryResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    private ProductHistoryResponse wrapProductHistoryResponse(ProductEntity productEntity) {
        var seller = productEntity.getSeller();
        return ProductHistoryResponse.builder()
                .productId(productEntity.getId())
                .productName(productEntity.getName())
                .productThumbnail(productEntity.getThumbnail())
                .productDescription(productEntity.getDescription())
                .ownerId(seller.getId())
                .ownerName(seller.getShopName())
                .ownerAvatarUrl(seller.getAvatarUrl())
                .build();
    }


    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<ProductEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = productRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
