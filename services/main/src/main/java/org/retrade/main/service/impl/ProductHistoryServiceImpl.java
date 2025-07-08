package org.retrade.main.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.OrderStatusCodes;
import org.retrade.main.model.constant.ProductStatusEnum;
import org.retrade.main.model.dto.response.ProductHistoryResponse;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.OrderItemEntity;
import org.retrade.main.model.entity.ProductEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.repository.OrderItemRepository;
import org.retrade.main.repository.OrderStatusRepository;
import org.retrade.main.repository.ProductRepository;
import org.retrade.main.service.ProductHistoryService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductHistoryServiceImpl implements ProductHistoryService {
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final AuthUtils authUtils;
    private final OrderStatusRepository orderStatusRepository;

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

    @Override
    public void retradeProduct(String orderItemId) {
        var accountEntity = authUtils.getUserAccountFromAuthentication();
        validateProductSeller(accountEntity);
        var orderItemEntity = orderItemRepository.findById(orderItemId).orElseThrow(() -> new IllegalArgumentException("Order item not found"));
        validateProductRetrade(orderItemEntity, accountEntity);
        var productEntity = orderItemEntity.getProduct();
        var productNewEntity = duplicateProduct(productEntity, accountEntity.getSeller());
        try {
            productRepository.save(productNewEntity);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to retrade product", ex);
        }
    }

    private void validateProductSeller(AccountEntity accountEntity) {
        var sellerEntity = accountEntity.getSeller();
        var role = authUtils.getRolesFromAuthUser();
        if (!role.contains("ROLE_SELLER")) {
            throw new ValidationException("User is not a seller of have been baned, please register seller or contact with Admin");
        }
        if (sellerEntity == null) {
            throw new ValidationException("User is not a seller, please register seller or contact with Admin");
        }
        if (sellerEntity.getVerified() == false) {
            throw new ValidationException("User is not verified. Please verify your account before retrading products.");
        }
    }

    private void validateProductRetrade(OrderItemEntity orderItemEntity, AccountEntity accountEntity) {
        var customerEntity = accountEntity.getCustomer();
        var orderEntity = orderItemEntity.getOrder();
        var orderCombo = orderItemEntity.getOrderCombo();
        var orderComboSeller = orderCombo.getSeller();
        var seller = accountEntity.getSeller();
        if (orderComboSeller.getId().equals(seller.getId())) {
            throw new ValidationException("Order item belongs to the same seller");
        }
        if (!orderEntity.getCustomer().getId().equals(customerEntity.getId())) {
            throw new ValidationException("Order item does not belong to customer");
        }
        var productEntity = orderItemEntity.getProduct();
        if (productEntity.getStatus() != ProductStatusEnum.DRAFT) {
            throw new ValidationException("Product is not in draft status");
        }
        if (productEntity.getQuantity() <= 0) {
            throw new ValidationException("Product quantity must be greater than 0");
        }
        var validStatusEntity = orderStatusRepository.findByCode(OrderStatusCodes.COMPLETED).orElseThrow(() -> new ValidationException("Order status not found"));
        if (!orderCombo.getOrderStatus().getId().equals(validStatusEntity.getId())) {
            throw new ValidationException("Order combo is not in completed status");
        }
    }

    private ProductEntity duplicateProduct(ProductEntity productEntity, SellerEntity seller) {
        return ProductEntity.builder()
                .name(productEntity.getName())
                .thumbnail(productEntity.getThumbnail())
                .description(productEntity.getDescription())
                .shortDescription(productEntity.getShortDescription())
                .productImages(productEntity.getProductImages())
                .avgVote(0.0)
                .brand(productEntity.getBrand())
                .parentProduct(productEntity)
                .warrantyExpiryDate(productEntity.getWarrantyExpiryDate())
                .condition(productEntity.getCondition())
                .model(productEntity.getModel())
                .currentPrice(productEntity.getCurrentPrice())
                .categories(productEntity.getCategories())
                .tags(productEntity.getTags())
                .status(ProductStatusEnum.DRAFT)
                .verified(true)
                .seller(seller)
                .quantity(productEntity.getQuantity())
                .build();
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
