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
import org.retrade.main.repository.jpa.OrderItemRepository;
import org.retrade.main.repository.jpa.OrderStatusRepository;
import org.retrade.main.repository.jpa.SellerRepository;
import org.retrade.main.service.RevenueService;
import org.retrade.main.util.AuthUtils;
import org.retrade.main.util.OrderStatusValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RevenueServiceImpl implements RevenueService {
    private final OrderComboRepository orderComboRepository;
    private final AuthUtils authUtils;
    private final OrderStatusValidator orderStatusValidator;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusRepository orderStatusRepository;
    @Override
    public PaginationWrapper<List<RevenueResponse>> getMyRevenue(QueryWrapper queryWrapper) {
        var seller = getSeller();
        if(seller == null) {
            throw new ValidationException("user is not seller");
        }
        QueryFieldWrapper keyword = queryWrapper.search().remove("keyword");
        return orderComboRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("seller"), seller));
            Join<OrderComboEntity, OrderDestinationEntity> destinationJoin = root.join("orderDestination", JoinType.LEFT);
            Join<OrderComboEntity, OrderItemEntity> itemJoin = root.joinSet("orderItems", JoinType.LEFT);
            predicates.add(criteriaBuilder.equal(root.get("orderStatus").get("code"), OrderStatusCodes.COMPLETED));

            if (keyword != null && !keyword.getValue().toString().trim().isEmpty()) {
                String searchPattern = "%" + keyword.getValue().toString().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(destinationJoin.get("customerName")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(itemJoin.get("productName")), searchPattern)
                ));
            }

            return getOrderComboPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::wrapSellerRevenueResponse).stream().toList();
            return new PaginationWrapper.Builder<List<RevenueResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    private RevenueResponse wrapSellerRevenueResponse(OrderComboEntity combo) {
        var orderItems = combo.getOrderItems();
        var orderStatus = OrderStatusResponse.builder()
                .id(combo.getOrderStatus().getId())
                .code(combo.getOrderStatus().getCode())
                .name(combo.getOrderStatus().getName())
                .build();

        var orderDestination = combo.getOrderDestination();
        var orderDestinationResponse = wrapOrderDestinationResponse(orderDestination);
        var orderItemResponses = wrapCustomerOrderItemResponse(orderItems);
        return RevenueResponse.builder()
                .orderComboId(combo.getId())
                .createdDate(combo.getCreatedDate().toLocalDateTime())
                .destination(orderDestinationResponse)
                .items(orderItemResponses)
                .status(orderStatus)
                .totalPrice(combo.getGrandPrice())
                .feeAmount(getFeeAmount(combo.getGrandPrice()))
                .netAmount(getTotalAmount(combo.getGrandPrice()))
                .feePercent(getFeePercent(combo.getGrandPrice()))
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

    private double getFeePercent(BigDecimal grandPrice) {
        if (grandPrice.compareTo(BigDecimal.valueOf(500000)) < 0) return 0.05;
        if (grandPrice.compareTo(BigDecimal.valueOf(1000000)) <= 0) return 0.04;
        return 0.03;
    }

    private BigDecimal getTotalAmount(BigDecimal grandPrice) {
        var feePercent = getFeePercent(grandPrice);
        BigDecimal feeMultiplier = BigDecimal.ONE.subtract(BigDecimal.valueOf(feePercent));
        return grandPrice.multiply(feeMultiplier);
    }
    private BigDecimal getFeeAmount(BigDecimal grandPrice) {
        double feePercent = getFeePercent(grandPrice);
        return grandPrice.multiply(BigDecimal.valueOf(feePercent))
                .setScale(2, RoundingMode.HALF_UP);
    }



    private Predicate getOrderComboPredicate(Map<String, QueryFieldWrapper> param, Root<OrderComboEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
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
                () -> new ValidationException("order status not found")
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


    private SellerEntity getSeller() {
        return authUtils.getUserAccountFromAuthentication().getSeller();
    }
}
