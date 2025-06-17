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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderComboRepository orderComboRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CustomerContactRepository customerContactRepository;
    private final CartService cartService;
    private final VoucherGrpcClient voucherGrpcClient;
    private final OrderDestinationRepository orderDestinationRepository;
    private final AuthUtils authUtils;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    private static final Double DEFAULT_SHIPPING_COST = 25000.0;

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class}, propagation = Propagation.REQUIRED)
    public OrderResponse createOrder(CreateOrderRequest request) {
        CustomerEntity customer = getCurrentCustomerAccount();
        CustomerContactEntity contact = customerContactRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ValidationException("Contact not found"));
        validateCreateOrderRequest(request);
        var orderDestinationEntity = wrapOrderDestination(contact);
        List<ProductEntity> products = validateAndGetProducts(request.getProductIds());
        Map<SellerEntity, List<ProductEntity>> productsBySeller = groupProductsBySeller(products);
        BigDecimal subtotal = calculateSubtotal(products, request.getProductIds());
        BigDecimal taxTotal = calculateTax(subtotal);
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = subtotal.add(taxTotal).subtract(discountTotal)
                .add(BigDecimal.valueOf(DEFAULT_SHIPPING_COST));
        OrderEntity order = createOrderEntity(customer, subtotal, taxTotal, discountTotal, grandTotal, orderDestinationEntity);
        OrderEntity savedOrder = orderRepository.save(order);
        List<OrderComboEntity> orderCombos = createOrderCombos(savedOrder, productsBySeller);
        createOrderItems(savedOrder, products, orderCombos);
        createOrderHistory(savedOrder, "Order created", customer.getId());
        cartService.clearCart();
        return mapToOrderResponse(savedOrder);
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
        var customerEntity = order.getCustomer();
        OrderStatusEntity newStatus = orderStatusRepository.findByCode(statusCode)
                .orElseThrow(() -> new ValidationException("Order status not found: " + statusCode));

        List<OrderComboEntity> orderCombos = orderComboRepository.findByOrderDestination(order.getOrderDestination());
        for (OrderComboEntity combo : orderCombos) {
            combo.setOrderStatus(newStatus);
            orderComboRepository.save(combo);
        }

        createOrderHistory(order, notes != null ? notes : "Status updated to " + newStatus.getName(), customerEntity.getLastName());

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public void cancelOrder(String orderId, String reason) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Order not found with ID: " + orderId));
        var customerEntity = order.getCustomer();
        OrderStatusEntity cancelledStatus = orderStatusRepository.findByCode("CANCELLED")
                .orElseThrow(() -> new ValidationException("Cancelled order status not found"));

        List<OrderComboEntity> orderCombos = orderComboRepository.findByOrderDestination(order.getOrderDestination());
        for (OrderComboEntity combo : orderCombos) {
            combo.setOrderStatus(cancelledStatus);
            orderComboRepository.save(combo);
        }

        createOrderHistory(order, "Order cancelled: " + (reason != null ? reason : "No reason provided"), customerEntity.getLastName());
    }

    private CustomerEntity getCurrentCustomerAccount() {
        var account = authUtils.getUserAccountFromAuthentication();
        var customerEntity = account.getCustomer();
        if (customerEntity == null) {
            throw new ValidationException("User is not a customer");
        }
        return customerEntity;
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
            //log.error("Error validating voucher: {}", e.getMessage(), e);
            throw new ActionFailedException("Failed to validate voucher", e);
        }
    }

    private void applyVoucher(String voucherCode, String accountId, String orderId, BigDecimal orderTotal) {
        try {
            ApplyVoucherResponse response = voucherGrpcClient.applyVoucher(voucherCode, accountId, orderId, orderTotal);
        } catch (Exception e) {
        }
    }

    private OrderEntity createOrderEntity(CustomerEntity customer, BigDecimal subtotal, BigDecimal taxTotal,
                                          BigDecimal discountTotal, BigDecimal grandTotal, OrderDestinationEntity orderDestination) {
        return OrderEntity.builder()
                .customer(customer)
                .subtotal(subtotal)
                .taxTotal(taxTotal)
                .discountTotal(discountTotal)
                .shippingCost(DEFAULT_SHIPPING_COST)
                .grandTotal(grandTotal)
                .orderDestination(orderDestination)
                .build();
    }


    private List<OrderComboEntity> createOrderCombos(OrderEntity order, Map<SellerEntity, List<ProductEntity>> productsBySeller) {
        OrderStatusEntity pendingStatus = orderStatusRepository.findByCode("PENDING")
                .orElseThrow(() -> new ValidationException("Pending order status not found"));
        List<OrderComboEntity> orderCombos = new ArrayList<>();
        OrderDestinationEntity orderDestination = order.getOrderDestination();
        if (orderDestination == null) {
            throw new ValidationException("Order destination is required for creating order combos");
        }
        for (Map.Entry<SellerEntity, List<ProductEntity>> entry : productsBySeller.entrySet()) {
            SellerEntity seller = entry.getKey();
            List<ProductEntity> sellerProducts = entry.getValue();
            BigDecimal sellerTotal = sellerProducts.stream()
                    .map(ProductEntity::getCurrentPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            OrderComboEntity combo = OrderComboEntity.builder()
                    .seller(seller)
                    .grandPrice(sellerTotal)
                    .orderDestination(orderDestination)
                    .orderStatus(pendingStatus)
                    .build();
            OrderComboEntity savedCombo = orderComboRepository.save(combo);
            orderCombos.add(savedCombo);
        }
        if (orderCombos.isEmpty()) {
            throw new ActionFailedException("Failed to create any order combos");
        }
        return orderCombos;
    }

    private void createOrderItems(OrderEntity order, List<ProductEntity> products, List<OrderComboEntity> orderCombos) {
        if (orderCombos == null || orderCombos.isEmpty()) {
            throw new ValidationException("No order combos found for order");
        }
        Map<SellerEntity, OrderComboEntity> sellerComboMap = orderCombos.stream()
                .collect(Collectors.toMap(
                        OrderComboEntity::getSeller,
                        combo -> combo,
                        (existing, replacement) -> existing));
        for (ProductEntity product : products) {
            if (product == null) {
                continue;
            }
            OrderComboEntity combo = sellerComboMap.get(product.getSeller());
            if (combo == null) {
                throw new ValidationException("No order combo found for seller: " + product.getSeller().getId());
            }
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

    @Transactional(readOnly = true)
    public OrderResponse mapToOrderResponse(OrderEntity order) {
        if (order == null) {
            return null;
        }
        String status = null;
        if (order.getOrderDestination() != null) {
            status = getOrderStatusFromDestination(order.getOrderDestination());
        }
        return OrderResponse.builder()
                .orderId(order.getId())
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerName(order.getCustomer() != null ?
                        (order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName()) : null)
                .destination(mapToOrderDestinationResponse(order.getOrderDestination()))
                .items(mapToOrderItemResponses(order))
                .orderCombos(mapToOrderComboResponses(order))
                .subtotal(order.getSubtotal())
                .taxTotal(order.getTaxTotal())
                .discountTotal(order.getDiscountTotal())
                .shippingCost(order.getShippingCost())
                .grandTotal(order.getGrandTotal())
                .status(status)
                .createdAt(order.getCreatedDate() != null ? order.getCreatedDate().toLocalDateTime() : null)
                .updatedAt(order.getUpdatedDate() != null ? order.getUpdatedDate().toLocalDateTime() : null)
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public String getOrderStatusFromDestination(OrderDestinationEntity orderDestination) {
        try {
            List<OrderComboEntity> combos = orderComboRepository.findByOrderDestination(orderDestination);
            if (combos != null && !combos.isEmpty() && combos.get(0).getOrderStatus() != null) {
                return combos.get(0).getOrderStatus().getName();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
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
                .addressLine(destination.getAddressLine())
                .build();
    }

    private List<OrderItemResponse> mapToOrderItemResponses(OrderEntity order) {
        if (order == null) {
            return Collections.emptyList();
        }
        Set<OrderItemEntity> orderItems = order.getOrderItems();
        if (orderItems == null || orderItems.isEmpty()) {
            return Collections.emptyList();
        }
        return orderItems.stream()
                .map(this::mapToOrderItemResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItemEntity orderItem) {
        if (orderItem == null || orderItem.getProduct() == null) {
            throw new ValidationException("Order item or associated product is null");
        }
        ProductEntity product = orderItem.getProduct();
        SellerEntity seller = product.getSeller();
        if (seller == null) {
            throw new ValidationException("Seller not found for product: " + product.getId());
        }
        BigDecimal quantity = BigDecimal.ONE;
        BigDecimal unitPrice = orderItem.getBasePrice();
        BigDecimal totalPrice = unitPrice.multiply(quantity);
        return OrderItemResponse.builder()
                .productId(product.getId())
                .productName(orderItem.getProductName())
                .productThumbnail(orderItem.getBackgroundUrl())
                .sellerName(seller.getShopName())
                .sellerId(seller.getId())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .shortDescription(orderItem.getShortDescription())
                .build();
    }

    private List<OrderComboResponse> mapToOrderComboResponses(OrderEntity order) {
        if (order == null || order.getOrderDestination() == null) {
            return Collections.emptyList();
        }
        List<OrderComboEntity> orderCombos = orderComboRepository.findByOrderDestination(order.getOrderDestination());
        if (orderCombos == null || orderCombos.isEmpty()) {
            return Collections.emptyList();
        }
        return orderCombos.stream()
                .map(this::mapToOrderComboResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    OrderComboResponse mapToOrderComboResponse(OrderComboEntity combo) {
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

    private OrderDestinationEntity wrapOrderDestination(CustomerContactEntity customerContactEntity) {
        return OrderDestinationEntity.builder()
                .customerName(customerContactEntity.getCustomerName())
                .phone(customerContactEntity.getPhone())
                .state(customerContactEntity.getState())
                .country(customerContactEntity.getCountry())
                .district(customerContactEntity.getDistrict())
                .ward(customerContactEntity.getWard())
                .addressLine(customerContactEntity.getAddressLine())
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