package org.retrade.main.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.client.VoucherGrpcClient;
import org.retrade.main.model.dto.request.CreateOrderRequest;
import org.retrade.main.model.dto.response.OrderComboResponse;
import org.retrade.main.model.dto.response.OrderDestinationResponse;
import org.retrade.main.model.dto.response.OrderItemResponse;
import org.retrade.main.model.dto.response.OrderResponse;
import org.retrade.main.model.entity.*;
import org.retrade.main.repository.*;
import org.retrade.main.service.CartService;
import org.retrade.main.service.OrderService;
import org.retrade.main.util.AuthUtils;
import org.retrade.proto.voucher.ApplyVoucherResponse;
import org.retrade.proto.voucher.ValidateVoucherResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderComboRepository orderComboRepository;
    private final OrderDestinationRepository orderDestinationRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CartService cartService;
    private final VoucherGrpcClient voucherGrpcClient;
    private final AuthUtils authUtils;
    
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    private static final Double DEFAULT_SHIPPING_COST = 25000.0;
    
    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for products: {}", request.getProductIds());
        
        String accountId = getCurrentAccountId();
        CustomerEntity customer = getCustomerByAccountId(accountId);
        
        validateCreateOrderRequest(request);
        
        List<ProductEntity> products = validateAndGetProducts(request.getProductIds());
        
        Map<SellerEntity, List<ProductEntity>> productsBySeller = groupProductsBySeller(products);
        
        BigDecimal subtotal = calculateSubtotal(products, request.getProductIds());
        BigDecimal taxTotal = calculateTax(subtotal);
        
        ValidateVoucherResponse voucherValidation = null;
        BigDecimal discountTotal = BigDecimal.ZERO;
        
        if (StringUtils.hasText(request.getVoucherCode())) {
            voucherValidation = validateVoucher(request.getVoucherCode(), accountId, subtotal, request.getProductIds());
            if (voucherValidation.getValid()) {
                discountTotal = BigDecimal.valueOf(voucherValidation.getDiscountAmount());
            } else {
                throw new ValidationException("Voucher validation failed: " + voucherValidation.getMessage());
            }
        }
        
        BigDecimal grandTotal = subtotal.add(taxTotal).subtract(discountTotal).add(BigDecimal.valueOf(DEFAULT_SHIPPING_COST));
        
        OrderEntity order = createOrderEntity(customer, subtotal, taxTotal, discountTotal, grandTotal, request);
        OrderEntity savedOrder = orderRepository.save(order);
        
        createOrderDestination(savedOrder, request);
        
        List<OrderComboEntity> orderCombos = createOrderCombos(savedOrder, productsBySeller);
        
        createOrderItems(savedOrder, products, request.getProductIds(), orderCombos);
        
        if (voucherValidation != null && voucherValidation.getValid()) {
            applyVoucher(request.getVoucherCode(), accountId, savedOrder.getId(), grandTotal);
        }
        
        createOrderHistory(savedOrder, "Order created", getCurrentAccountId());
        
        cartService.clearCart();
        
        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return mapToOrderResponse(savedOrder);
    }
    
    private String getCurrentAccountId() {
        return authUtils.getUserAccountFromAuthentication().getId();
    }
    
    private CustomerEntity getCustomerByAccountId(String accountId) {
        return customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ValidationException("Customer profile not found"));
    }
    
    private void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request.getProductIds().isEmpty()) {
            throw new ValidationException("Product list cannot be empty");
        }
        
        if (request.getProductIds().size() > 100) {
            throw new ValidationException("Cannot order more than 100 different products at once");
        }
    }
    
    private List<ProductEntity> validateAndGetProducts(List<String> productIds) {
        List<ProductEntity> products = new ArrayList<>();
        List<String> notFoundProducts = new ArrayList<>();
        
        for (String productId : productIds) {
            Optional<ProductEntity> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                ProductEntity product = productOpt.get();
                if (!product.getVerified()) {
                    throw new ValidationException("Product " + productId + " is not verified and cannot be ordered");
                }
                products.add(product);
            } else {
                notFoundProducts.add(productId);
            }
        }
        
        if (!notFoundProducts.isEmpty()) {
            throw new ValidationException("Products not found: " + String.join(", ", notFoundProducts));
        }
        
        return products;
    }
    
    private Map<SellerEntity, List<ProductEntity>> groupProductsBySeller(List<ProductEntity> products) {
        return products.stream()
                .collect(Collectors.groupingBy(ProductEntity::getSeller));
    }
    
    private BigDecimal calculateSubtotal(List<ProductEntity> products, List<String> productIds) {
        Map<String, Long> productQuantities = productIds.stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));
        
        return products.stream()
                .map(product -> {
                    Long quantity = productQuantities.get(product.getId());
                    return product.getCurrentPrice().multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }
    
    private ValidateVoucherResponse validateVoucher(String voucherCode, String accountId, BigDecimal orderTotal, List<String> productIds) {
        try {
            return voucherGrpcClient.validateVoucher(voucherCode, accountId, orderTotal, productIds);
        } catch (Exception e) {
            log.error("Error validating voucher: {}", e.getMessage(), e);
            throw new ActionFailedException("Failed to validate voucher", e);
        }
    }
    
    private void applyVoucher(String voucherCode, String accountId, String orderId, BigDecimal orderTotal) {
        try {
            ApplyVoucherResponse response = voucherGrpcClient.applyVoucher(voucherCode, accountId, orderId, orderTotal);
            if (!response.getSuccess()) {
                log.warn("Failed to apply voucher {}: {}", voucherCode, response.getMessage());
            }
        } catch (Exception e) {
            log.error("Error applying voucher: {}", e.getMessage(), e);
        }
    }

    private OrderEntity createOrderEntity(CustomerEntity customer, BigDecimal subtotal, BigDecimal taxTotal,
                                        BigDecimal discountTotal, BigDecimal grandTotal, CreateOrderRequest request) {
        return OrderEntity.builder()
                .customer(customer)
                .subtotal(subtotal)
                .taxTotal(taxTotal)
                .discountTotal(discountTotal)
                .shippingCost(DEFAULT_SHIPPING_COST)
                .grandTotal(grandTotal)
                .build();
    }

    private void createOrderDestination(OrderEntity order, CreateOrderRequest request) {
        OrderDestinationEntity destination = OrderDestinationEntity.builder()
                .order(order)
                .customerName(request.getDeliveryAddress().getCustomerName())
                .phone(request.getDeliveryAddress().getPhone())
                .state(request.getDeliveryAddress().getState())
                .country(request.getDeliveryAddress().getCountry())
                .district(request.getDeliveryAddress().getDistrict())
                .ward(request.getDeliveryAddress().getWard())
                .address(request.getDeliveryAddress().getAddressLine())
                .build();

        orderDestinationRepository.save(destination);
        order.setOrderDestination(destination);
    }

    private List<OrderComboEntity> createOrderCombos(OrderEntity order, Map<SellerEntity, List<ProductEntity>> productsBySeller) {
        OrderStatusEntity pendingStatus = orderStatusRepository.findByCode("PENDING")
                .orElseThrow(() -> new ValidationException("Pending order status not found"));

        List<OrderComboEntity> orderCombos = new ArrayList<>();

        for (Map.Entry<SellerEntity, List<ProductEntity>> entry : productsBySeller.entrySet()) {
            SellerEntity seller = entry.getKey();
            List<ProductEntity> sellerProducts = entry.getValue();

            BigDecimal sellerTotal = sellerProducts.stream()
                    .map(ProductEntity::getCurrentPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            OrderComboEntity combo = OrderComboEntity.builder()
                    .seller(seller)
                    .grandPrice(sellerTotal)
                    .orderDestination(order.getOrderDestination())
                    .orderStatus(pendingStatus)
                    .build();

            OrderComboEntity savedCombo = orderComboRepository.save(combo);
            orderCombos.add(savedCombo);
        }

        return orderCombos;
    }

    private void createOrderItems(OrderEntity order, List<ProductEntity> products, List<String> productIds,
                                List<OrderComboEntity> orderCombos) {
        Map<String, Long> productQuantities = productIds.stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

        Map<SellerEntity, OrderComboEntity> sellerComboMap = orderCombos.stream()
                .collect(Collectors.toMap(OrderComboEntity::getSeller, combo -> combo));

        for (ProductEntity product : products) {
            OrderComboEntity combo = sellerComboMap.get(product.getSeller());

            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .product(product)
                    .orderCombo(combo)
                    .productName(product.getName())
                    .shortDescription(product.getShortDescription())
                    .backgroundUrl(product.getThumbnail())
                    .basePrice(product.getCurrentPrice())
                    .unit("pcs")
                    .build();

            orderItemRepository.save(orderItem);
        }
    }

    private void createOrderHistory(OrderEntity order, String notes, String createdBy) {
        OrderHistoryEntity history = OrderHistoryEntity.builder()
                .order(order)
                .status(true)
                .notes(notes)
                .createdBy(createdBy)
                .build();

        orderHistoryRepository.save(history);
    }

    @Override
    public OrderResponse getOrderById(String orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Order not found with ID: " + orderId));
        return mapToOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByCustomer(String customerId) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ValidationException("Customer not found with ID: " + customerId));

        List<OrderEntity> orders = orderRepository.findByCustomer(customer);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaginationWrapper<List<OrderResponse>> getAllOrders(QueryWrapper queryWrapper) {
        return orderRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            return getPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::mapToOrderResponse).stream().toList();
            return new PaginationWrapper.Builder<List<OrderResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public OrderResponse updateOrderStatus(String orderId, String statusCode, String notes) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Order not found with ID: " + orderId));

        OrderStatusEntity newStatus = orderStatusRepository.findByCode(statusCode)
                .orElseThrow(() -> new ValidationException("Order status not found: " + statusCode));

        List<OrderComboEntity> orderCombos = orderComboRepository.findByOrderDestination(order.getOrderDestination());
        for (OrderComboEntity combo : orderCombos) {
            combo.setOrderStatus(newStatus);
            orderComboRepository.save(combo);
        }

        createOrderHistory(order, notes != null ? notes : "Status updated to " + newStatus.getName(), getCurrentAccountId());

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public void cancelOrder(String orderId, String reason) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Order not found with ID: " + orderId));

        OrderStatusEntity cancelledStatus = orderStatusRepository.findByCode("CANCELLED")
                .orElseThrow(() -> new ValidationException("Cancelled order status not found"));

        List<OrderComboEntity> orderCombos = orderComboRepository.findByOrderDestination(order.getOrderDestination());
        for (OrderComboEntity combo : orderCombos) {
            combo.setOrderStatus(cancelledStatus);
            orderComboRepository.save(combo);
        }

        createOrderHistory(order, "Order cancelled: " + (reason != null ? reason : "No reason provided"), getCurrentAccountId());
    }

    private OrderResponse mapToOrderResponse(OrderEntity order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName())
                .destination(mapToOrderDestinationResponse(order.getOrderDestination()))
                .items(mapToOrderItemResponses(order))
                .orderCombos(mapToOrderComboResponses(order))
                .subtotal(order.getSubtotal())
                .taxTotal(order.getTaxTotal())
                .discountTotal(order.getDiscountTotal())
                .shippingCost(order.getShippingCost())
                .grandTotal(order.getGrandTotal())
                .createdAt(order.getCreatedDate().toLocalDateTime())
                .updatedAt(order.getUpdatedDate().toLocalDateTime())
                .build();
    }

    private OrderDestinationResponse mapToOrderDestinationResponse(OrderDestinationEntity destination) {
        if (destination == null) {
            return null;
        }

        return OrderDestinationResponse.builder()
                .customerName(destination.getCustomerName())
                .phone(destination.getPhone())
                .state(destination.getState())
                .country(destination.getCountry())
                .district(destination.getDistrict())
                .ward(destination.getWard())
                .addressLine(destination.getAddress())
                .build();
    }

    private List<OrderItemResponse> mapToOrderItemResponses(OrderEntity order) {
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrder(order);

        return orderItems.stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItemEntity orderItem) {
        return OrderItemResponse.builder()
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProductName())
                .productThumbnail(orderItem.getBackgroundUrl())
                .sellerName(orderItem.getProduct().getSeller().getShopName())
                .sellerId(orderItem.getProduct().getSeller().getId())
                .unitPrice(orderItem.getBasePrice())
                .totalPrice(orderItem.getBasePrice())
                .shortDescription(orderItem.getShortDescription())
                .build();
    }

    private List<OrderComboResponse> mapToOrderComboResponses(OrderEntity order) {
        List<OrderComboEntity> orderCombos = orderComboRepository.findByOrderDestination(order.getOrderDestination());

        return orderCombos.stream()
                .map(this::mapToOrderComboResponse)
                .collect(Collectors.toList());
    }

    private OrderComboResponse mapToOrderComboResponse(OrderComboEntity combo) {
        List<OrderItemEntity> comboItems = orderItemRepository.findByOrderCombo(combo);
        List<String> itemIds = comboItems.stream()
                .map(OrderItemEntity::getId)
                .collect(Collectors.toList());

        return OrderComboResponse.builder()
                .comboId(combo.getId())
                .sellerId(combo.getSeller().getId())
                .sellerName(combo.getSeller().getShopName())
                .grandPrice(combo.getGrandPrice())
                .status(combo.getOrderStatus().getName())
                .itemIds(itemIds)
                .build();
    }

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<OrderEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = orderRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
