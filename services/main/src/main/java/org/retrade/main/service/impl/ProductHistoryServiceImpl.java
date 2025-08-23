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
import org.retrade.main.model.document.ProductDocument;
import org.retrade.main.model.dto.request.CreateRetradeRequest;
import org.retrade.main.model.dto.response.CreateRetradeResponse;
import org.retrade.main.model.dto.response.ProductHistoryResponse;
import org.retrade.main.model.entity.*;
import org.retrade.main.repository.elasticsearch.ProductElasticsearchRepository;
import org.retrade.main.repository.jpa.OrderItemRepository;
import org.retrade.main.repository.jpa.OrderStatusRepository;
import org.retrade.main.repository.jpa.ProductRepository;
import org.retrade.main.repository.jpa.ReTradeRecordRepository;
import org.retrade.main.service.ProductHistoryService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductHistoryServiceImpl implements ProductHistoryService {
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReTradeRecordRepository reTradeRecordRepository;
    private final AuthUtils authUtils;
    private final OrderStatusRepository orderStatusRepository;
    private final ProductElasticsearchRepository productElasticsearchRepository;

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

    @Transactional(rollbackFor = {ValidationException.class, ActionFailedException.class, Exception.class}, isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public CreateRetradeResponse createRetradeProduct(CreateRetradeRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        validateProductSeller(account);
        var seller = account.getSeller();
        var orderItem = orderItemRepository.findById(request.getOrderItemId()).orElseThrow(() -> new ValidationException("Không tìm thấy sản phẩm trong đơn hàng"));
        validateProductRetrade(orderItem, account, request);
        var productEntity = orderItem.getProduct();
        var retradeProduct = duplicateProduct(productEntity, seller, request);
        try {
            var result = productRepository.save(retradeProduct);
            var recordEntity = ReTradeRecordEntity.builder()
                    .orderItem(orderItem)
                    .product(result)
                    .quantity(request.getQuantity())
                    .build();
            var resultRecord = reTradeRecordRepository.save(recordEntity);
            saveProductToElasticSearch(result);
            return CreateRetradeResponse.builder()
                    .productId(productEntity.getId())
                    .retradeProductId(retradeProduct.getId())
                    .retradeRecordedId(resultRecord.getId())
                    .build();
        } catch (Exception ex) {
            throw new ActionFailedException("Tạo sản phẩm trao đổi lại thất bại", ex);
        }
    }

    private void validateProductSeller(AccountEntity accountEntity) {
        var sellerEntity = accountEntity.getSeller();
        var role = authUtils.getRolesFromAuthUser();
        if (!role.contains("ROLE_SELLER")) {
            throw new ValidationException("Người dùng không phải là người bán hoặc đã bị cấm, vui lòng đăng ký bán hàng hoặc liên hệ với Quản trị viên");
        }
        if (sellerEntity == null) {
            throw new ValidationException("Người dùng không phải là người bán hoặc đã bị cấm, vui lòng đăng ký bán hàng hoặc liên hệ với Quản trị viên");
        }
        if (sellerEntity.getVerified() == false) {
            throw new ValidationException("Người dùng chưa được xác minh. Vui lòng xác minh tài khoản của bạn trước khi trao đổi lại sản phẩm");
        }
    }

    private void validateProductRetrade(OrderItemEntity orderItemEntity, AccountEntity accountEntity, CreateRetradeRequest request) {
        var customerEntity = accountEntity.getCustomer();
        var orderEntity = orderItemEntity.getOrder();
        var orderCombo = orderItemEntity.getOrderCombo();
        var orderComboSeller = orderCombo.getSeller();
        var seller = accountEntity.getSeller();
        if (orderComboSeller.getId().equals(seller.getId())) {
            throw new ValidationException("Sản phẩm trong đơn hàng thuộc cùng một người bán");
        }
        if (!orderEntity.getCustomer().getId().equals(customerEntity.getId())) {
            throw new ValidationException("Sản phẩm trong đơn hàng không thuộc về khách hàng");
        }
        var validStatusEntity = orderStatusRepository.findByCode(OrderStatusCodes.COMPLETED).orElseThrow(() -> new ValidationException("OKhông tìm thấy trạng thái đơn hàng"));
        if (!orderCombo.getOrderStatus().getId().equals(validStatusEntity.getId())) {
            throw new ValidationException("Đơn hàng chưa ở trạng thái hoàn thành");
        }
        var buyerId = orderItemEntity.getOrder().getCustomer().getId();
        if (!buyerId.equals(accountEntity.getCustomer().getId())) {
            throw new ValidationException("Sản phẩm trong đơn hàng không thuộc về khách hàng");
        }
        var totalBought = orderItemEntity.getQuantity();
        Integer alreadyRetrade = reTradeRecordRepository.sumQuantityByOrderItemId(request.getOrderItemId());
        if (alreadyRetrade == null) {
            alreadyRetrade = 0;
        }
        int available = totalBought - alreadyRetrade;
        if (available <= 0) {
            throw new ValidationException("Không còn sản phẩm nào có thể trao đổi lại");
        }
        if (request.getQuantity() > available) {
            throw new ValidationException("Số lượng yêu cầu vượt quá số lượng có thể trao đổi lại");
        }
    }

    private ProductEntity duplicateProduct(ProductEntity productEntity, SellerEntity seller, CreateRetradeRequest request) {
        var productBuilder =  ProductEntity.builder()
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
                .categories(new HashSet<>(productEntity.getCategories()))
                .tags(productEntity.getTags())
                .status(ProductStatusEnum.DRAFT)
                .verified(false)
                .seller(seller)
                .quantity(productEntity.getQuantity());
        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(productEntity.getCurrentPrice()) > 0) {
                throw new ValidationException("Giá yêu cầu cao hơn giá hiện tại");
            }
            if (request.getShortDescription() != null && !request.getShortDescription().isBlank()) {
                productBuilder.shortDescription(request.getShortDescription());
            }
            if (request.getDescription() != null && !request.getDescription().isBlank()) {
                productBuilder.description(request.getDescription());
            }
            if (request.getThumbnail() != null && !request.getThumbnail().isBlank()) {
                productBuilder.thumbnail(request.getThumbnail());
            }
            productBuilder.currentPrice(request.getPrice());
        }
        return productBuilder.build();
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

    private void saveProductToElasticSearch(ProductEntity productEntity) {
        var productDoc = ProductDocument.wrapEntityToDocument(productEntity);
        productElasticsearchRepository.save(productDoc);
    }

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<ProductEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = productRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
