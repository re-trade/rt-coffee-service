package org.retrade.main.service.impl;

import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.OrderStatusCodes;
import org.retrade.main.model.dto.response.*;
import org.retrade.main.model.entity.*;
import org.retrade.main.repository.jpa.OrderComboRepository;
import org.retrade.main.repository.jpa.OrderStatusRepository;
import org.retrade.main.repository.jpa.SellerRevenueRepository;
import org.retrade.main.service.RevenueService;
import org.retrade.main.util.AuthUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RevenueServiceImpl implements RevenueService {
    private final OrderComboRepository orderComboRepository;
    private final AuthUtils authUtils;
    private final SellerRevenueRepository sellerRevenueRepository;
    private final OrderStatusRepository orderStatusRepository;

    @Override
    public PaginationWrapper<List<RevenueResponse>> getMyRevenue(QueryWrapper queryWrapper) {
        SellerEntity seller = getSeller();
        if (seller == null) {
            throw new ValidationException("Người dùng không phải là người bán");
        }
        QueryFieldWrapper keyword = queryWrapper.search().remove("keyword");
        QueryFieldWrapper orderStatus = queryWrapper.search().remove("orderStatus");
        return sellerRevenueRepository.query(queryWrapper,
                (param) -> buildRevenuePredicate(param, seller, keyword, orderStatus),
                this::mapToPaginationWrapper
        );
    }

    private Predicate getSellerRevenuePredicate(Map<String, QueryFieldWrapper> param, Root<SellerRevenueEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = orderComboRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    @Override
    public RevenueStatResponse getStatsRevenue() {
        var seller = getSeller();
        OrderStatusEntity orderStatus = orderStatusRepository.findByCode(OrderStatusCodes.COMPLETED).orElseThrow(
                () -> new ValidationException("Không tìm thấy trạng thái đơn hàng")
        );
        var totalPrice = orderComboRepository.getTotalPriceAfterFeeBySellerAndStatus(seller, orderStatus);
        var totalOrder = orderComboRepository.countOrdersBySellerAndStatus(seller, orderStatus);
        var aov = orderComboRepository.getAverageGrandPriceBySellerAndStatus(seller, orderStatus);
        var totalItemsSold =orderComboRepository.getTotalItemsSoldBySellerAndStatus(seller, orderStatus);
        return RevenueStatResponse.builder()
                .totalRevenue(totalPrice)
                .totalOrder(totalOrder)
                .averageOrderValue(aov)
                .totalItemsSold(totalItemsSold)
                .build();
    }

    private Specification<SellerRevenueEntity> buildRevenuePredicate(Map<String, QueryFieldWrapper> param, SellerEntity seller, QueryFieldWrapper keyword, QueryFieldWrapper orderStatus) {
        return (root, query, cb) -> {
            assert query != null;
            query.distinct(true);
            query.orderBy(cb.desc(root.get("updatedDate")));

            List<Predicate> predicates = new ArrayList<>();
            Join<SellerRevenueEntity, OrderComboEntity> orderComboJoin = root.join("orderCombo", JoinType.INNER);

            predicates.add(cb.equal(orderComboJoin.get("seller"), seller));
            predicates.add(cb.equal(orderComboJoin.get("orderStatus").get("code"), OrderStatusCodes.COMPLETED));

            if (hasKeyword(keyword)) {
                predicates.add(buildKeywordPredicate(query, cb, orderComboJoin, keyword.getValue().toString()));
            }
            if (hasOrderStatus(orderStatus)) {
                predicates.add(cb.equal(orderComboJoin.get("orderStatus").get("code"), orderStatus.getValue().toString()));
            }
            return getSellerRevenuePredicate(param, root, cb, predicates);
        };
    }

    private boolean hasKeyword(QueryFieldWrapper keyword) {
        return keyword != null && keyword.getValue() != null && !keyword.getValue().toString().trim().isEmpty();
    }

    private boolean hasOrderStatus(QueryFieldWrapper orderStatus) {
        return orderStatus != null && orderStatus.getValue() != null && !orderStatus.getValue().toString().trim().isEmpty();
    }

    private Predicate buildKeywordPredicate(CriteriaQuery<?> query, CriteriaBuilder cb, Join<?, ?> orderComboJoin, String keyword) {
        String searchPattern = "%" + keyword.trim().toLowerCase() + "%";

        Subquery<String> destinationSub = query.subquery(String.class);
        Root<OrderDestinationEntity> destRoot = destinationSub.from(OrderDestinationEntity.class);
        destinationSub.select(destRoot.get("id"))
                .where(cb.like(cb.lower(destRoot.get("customerName")), searchPattern));

        Subquery<String> itemSub = query.subquery(String.class);
        Root<OrderItemEntity> itemRoot = itemSub.from(OrderItemEntity.class);
        itemSub.select(itemRoot.get("orderCombo").get("id"))
                .where(cb.like(cb.lower(itemRoot.get("productName")), searchPattern));

        return cb.or(
                orderComboJoin.get("orderDestination").get("id").in(destinationSub),
                orderComboJoin.get("id").in(itemSub)
        );
    }

    private PaginationWrapper<List<RevenueResponse>> mapToPaginationWrapper(Page<SellerRevenueEntity> items) {
        List<RevenueResponse> list = items.stream()
                .map(this::wrapRevenueFromEntity)
                .toList();

        return new PaginationWrapper.Builder<List<RevenueResponse>>()
                .setPaginationInfo(items)
                .setData(list)
                .build();
    }

    private RevenueResponse wrapRevenueFromEntity(SellerRevenueEntity revenue) {
        OrderComboEntity combo = revenue.getOrderCombo();

        return RevenueResponse.builder()
                .orderComboId(combo.getId())
                .createdDate(combo.getCreatedDate().toLocalDateTime())
                .destination(wrapOrderDestinationResponse(combo.getOrderDestination()))
                .items(wrapCustomerOrderItemResponse(combo.getOrderItems()))
                .status(OrderStatusResponse.builder()
                        .id(combo.getOrderStatus().getId())
                        .code(combo.getOrderStatus().getCode())
                        .name(combo.getOrderStatus().getName())
                        .build())
                .totalPrice(revenue.getTotalAmount())
                .feePercent(revenue.getPlatformFeeRate())
                .feeAmount(revenue.getPlatformFeeAmount())
                .netAmount(revenue.getSellerRevenue())
                .build();
    }

    private OrderDestinationResponse wrapOrderDestinationResponse(OrderDestinationEntity orderDestination) {
        return OrderDestinationResponse.builder()
                .customerName(orderDestination.getCustomerName())
                .phone(orderDestination.getPhone())
                .state(orderDestination.getState())
                .country(orderDestination.getCountry())
                .district(orderDestination.getDistrict())
                .ward(orderDestination.getWard())
                .addressLine(orderDestination.getAddressLine())
                .build();
    }

    private Set<CustomerOrderItemResponse> wrapCustomerOrderItemResponse(Set<OrderItemEntity> orderItems) {
        if (orderItems.isEmpty()) {
            return Collections.emptySet();
        }
        return orderItems.stream().map(item -> CustomerOrderItemResponse.builder()
                .itemId(item.getId())
                .itemThumbnail(item.getBackgroundUrl())
                .itemName(item.getProductName())
                .productId(item.getProduct().getId())
                .basePrice(item.getBasePrice())
                .quantity(item.getQuantity())
                .build()).collect(Collectors.toSet());
    }

    private SellerEntity getSeller() {
        return authUtils.getUserAccountFromAuthentication().getSeller();
    }
}
