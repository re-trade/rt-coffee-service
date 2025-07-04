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
import org.retrade.main.model.dto.request.CreateOrderRequest;
import org.retrade.main.model.dto.request.OrderItemRequest;
import org.retrade.main.model.dto.response.*;
import org.retrade.main.model.entity.*;
import org.retrade.main.repository.*;
import org.retrade.main.service.CartService;
import org.retrade.main.service.OrderService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final OrderStatusRepository orderStatusRepository;
    private final ProductRepository productRepository;
    private final OrderDestinationRepository orderDestinationRepository;
    private final CustomerRepository customerRepository;
    private final CustomerContactRepository customerContactRepository;
    private final CartService cartService;
    private final AuthUtils authUtils;
    
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    
    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public OrderResponse createOrder(CreateOrderRequest request) {
        CustomerEntity customer = getCurrentCustomerAccount();
        CustomerContactEntity contact = customerContactRepository.findById(request.getAddressId()).orElseThrow(() -> new ValidationException("Contact not found"));
        validateCreateOrderRequest(request);
        var orderDestinationEntity = wrapOrderDestination(contact);
        List<ProductEntity> products = validateAndGetProducts(request.getItems());
        
        Map<SellerEntity, List<ProductEntity>> productsBySeller = groupProductsBySeller(products);
        
        BigDecimal subtotal = calculateSubtotal(products);
        BigDecimal taxTotal = calculateTax(subtotal);
        BigDecimal discountTotal = BigDecimal.ZERO;

        BigDecimal grandTotal = subtotal.add(taxTotal).subtract(discountTotal);

        OrderEntity order = createOrderEntity(customer, subtotal, taxTotal, discountTotal, grandTotal);
        OrderEntity savedOrder;
        OrderDestinationEntity orderDestination;
        try {
            savedOrder = orderRepository.save(order);
            orderDestinationEntity.setOrder(savedOrder);
            orderDestination = orderDestinationRepository.save(orderDestinationEntity);
        } catch (Exception e) {
            throw new ActionFailedException("Failed to save order destination", e);
        }
        List<OrderComboEntity> orderCombos = createOrderCombos(productsBySeller, orderDestination);
        
        createOrderItems(savedOrder, products, orderCombos);

        cartService.clearCart();

        return mapToOrderResponse(savedOrder);
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
    @Transactional(readOnly = true)
    public PaginationWrapper<List<CustomerOrderComboResponse>> getCustomerOrderCombos(QueryWrapper queryWrapper) {
        var account = authUtils.getUserAccountFromAuthentication();
        var customerEntity = account.getCustomer();
        if (customerEntity == null) {
            throw new ValidationException("User is not a customer");
        }
        return orderComboRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("orderDestination").get("order").get("customer"),customerEntity));
            return getOrderComboPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::wrapCustomerOrderComboResponse).stream().toList();
            return new PaginationWrapper.Builder<List<CustomerOrderComboResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
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
    }
    @Override
    @Transactional(readOnly = true)
    public PaginationWrapper<List<CustomerOrderComboResponse>> getSellerOrderCombos(QueryWrapper queryFieldWrapper) {
        var account = authUtils.getUserAccountFromAuthentication();
        var seller = account.getSeller();
        if (seller == null) {
            throw new ValidationException("User is not a seller");
        }
        return orderComboRepository.query(queryFieldWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("seller"), seller));
            predicates.add(criteriaBuilder.notEqual(root.get("orderStatus").get("code"), "PENDING"));
            return getOrderComboPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::wrapCustomerOrderComboResponse).stream().toList();
            return new PaginationWrapper.Builder<List<CustomerOrderComboResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Transactional(readOnly = true)
    @Override
    public CustomerOrderComboResponse getSellerOrderComboById(String comboId) {
        var account = authUtils.getUserAccountFromAuthentication();
        var combo = getOrderComboById(comboId);
        var seller = account.getSeller();
        if (seller == null) {
            throw new ValidationException("User is not a seller");
        }
        var orderSeller = combo.getSeller();
        if (!seller.getId().equals(orderSeller.getId())) {
            throw new ValidationException("You are not the owner");
        }
        return wrapCustomerOrderComboResponse(combo);
    }

    @Transactional(readOnly = true)
    @Override
    public CustomerOrderComboResponse getCustomerOrderComboById(String comboId) {
        var account = authUtils.getUserAccountFromAuthentication();
        var combo = getOrderComboById(comboId);
        var customer = account.getCustomer();
        if (customer == null) {
            throw new ValidationException("User is not a customer");
        }
        var comboCustomer = combo.getOrderDestination().getOrder().getCustomer();
        if (!customer.getId().equals(comboCustomer.getId())) {
            throw new ValidationException("You are not the owner");
        }
        return wrapCustomerOrderComboResponse(combo);
    }

    private OrderComboEntity getOrderComboById(String comboId) {
        return orderComboRepository.findById(comboId)
                .orElseThrow(() -> new ValidationException("Order combo not found with ID: " + comboId));
    }

    @Override
    public List<OrderResponse> getOrdersByCurrentCustomer() {
        CustomerEntity customer = getCurrentCustomerAccount();

        List<OrderEntity> orders = orderRepository.findByCustomer(customer);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
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
        if (request.getItems().isEmpty()) {
            throw new ValidationException("Product list cannot be empty");
        }

        if (request.getItems().size() > 100) {
            throw new ValidationException("Cannot order more than 100 different products at once");
        }
    }

    private List<ProductEntity> validateAndGetProducts(List<OrderItemRequest> items) {
        List<ProductEntity> products = new ArrayList<>();
        List<String> notFoundProducts = new ArrayList<>();
        for (OrderItemRequest item : items) {
            var productId = item.getProductId();
            Optional<ProductEntity> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                ProductEntity product = productOpt.get();
                if (!product.getVerified()) {
                    throw new ValidationException("Product " + productId + " is not verified and cannot be ordered");
                }
                if (product.getQuantity() < item.getQuantity()) {
                    throw new ValidationException("Product " + productId + " has insufficient stock");
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

    private BigDecimal calculateSubtotal(List<ProductEntity> products) {
        return products.stream()
                .map(product -> {
                    return product.getCurrentPrice().multiply(BigDecimal.valueOf(product.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    private OrderEntity createOrderEntity(CustomerEntity customer, BigDecimal subtotal, BigDecimal taxTotal,
                                          BigDecimal discountTotal, BigDecimal grandTotal) {
        return OrderEntity.builder()
                .customer(customer)
                .subtotal(subtotal)
                .taxTotal(taxTotal)
                .discountTotal(discountTotal)
                .shippingCost(0.0)
                .grandTotal(grandTotal)
                .build();
    }

    private List<OrderComboEntity> createOrderCombos(Map<SellerEntity, List<ProductEntity>> productsBySeller, OrderDestinationEntity orderDestination) {
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
                    .orderDestination(orderDestination)
                    .orderStatus(pendingStatus)
                    .build();
            orderCombos.add(combo);
        }
        try {
            return orderComboRepository.saveAll(orderCombos);
        } catch (Exception ex) {
            throw new ActionFailedException("Failed to save order combos", ex);
        }
    }

    private void createOrderItems(OrderEntity order, List<ProductEntity> products,
                                  List<OrderComboEntity> orderCombos) {
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
                    .quantity(product.getQuantity())
                    .unit("vnd")
                    .build();

            orderItemRepository.save(orderItem);
        }
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
                .addressLine(destination.getAddressLine())
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

    private CustomerOrderComboResponse wrapCustomerOrderComboResponse(OrderComboEntity combo) {
        var orderItems = combo.getOrderItems();
        var seller = combo.getSeller();
        var orderStatus = combo.getOrderStatus();
        var orderDestination = combo.getOrderDestination();
        var orderDestinationResponse = wrapOrderDestinationResponse(orderDestination);
        var orderItemResponses = wrapCustomerOrderItemResponse(orderItems);
        return CustomerOrderComboResponse.builder()
                .comboId(combo.getId())
                .sellerId(seller.getId())
                .sellerAvatarUrl(seller.getAvatarUrl())
                .sellerName(seller.getShopName())
                .grandPrice(combo.getGrandPrice())
                .orderStatusId(orderStatus.getId())
                .orderStatus(orderStatus.getName())
                .items(orderItemResponses)
                .destination(orderDestinationResponse)
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

    private Predicate getPredicate(Map<String, QueryFieldWrapper> param, Root<OrderEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = orderRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate getOrderComboPredicate(Map<String, QueryFieldWrapper> param, Root<OrderComboEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = orderComboRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

}
