package org.retrade.main.service.impl;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.OrderStatusCodes;
import org.retrade.main.model.dto.request.CreateOrderRequest;
import org.retrade.main.model.dto.request.OrderItemRequest;
import org.retrade.main.model.dto.response.*;
import org.retrade.main.model.entity.*;
import org.retrade.main.repository.jpa.*;
import org.retrade.main.service.CartService;
import org.retrade.main.service.OrderService;
import org.retrade.main.util.AuthUtils;
import org.retrade.main.validator.OrderStatusValidator;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
    private final OrderStatusValidator  orderStatusValidator;
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public OrderResponse createOrder(CreateOrderRequest request) {
        CustomerEntity customer = getCurrentCustomerAccount();
        CustomerContactEntity contact = customerContactRepository.findById(request.getAddressId()).orElseThrow(() -> new ValidationException("Contact not found"));
        validateCreateOrderRequest(request);
        var orderDestinationEntity = wrapOrderDestination(contact);
        List<ProductEntity> products = validateAndGetProducts(request.getItems());
        subtractProductQuantities(products, request.getItems());

        Map<SellerEntity, List<ProductEntity>> productsBySeller = groupProductsBySeller(products);

        BigDecimal subtotal = calculateSubtotal(products, request.getItems());
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

        createOrderItems(savedOrder, products, orderCombos, request.getItems());

        cartService.clearCart();

        return mapToOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(String orderId) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getCustomer() == null) {
            throw new ValidationException("User is not a customer");
        }
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Order not found with ID: " + orderId));
        if (!order.getCustomer().getId().equals(account.getCustomer().getId())) {
            throw new ValidationException("You are not the owner");
        }
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
            var destinationJoin = root.join("orderDestination");
            var orderJoin = destinationJoin.join("order");
            var customerJoin = orderJoin.join("customer");
            predicates.add(criteriaBuilder.equal(customerJoin.get("id"), customerEntity.getId()));
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
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public void cancelOrderCustomer(String orderId, String reason) {
        var account = authUtils.getUserAccountFromAuthentication();
        var customerEntity = account.getCustomer();

        if (customerEntity == null) {
            throw new ValidationException("Account is not a customer");
        }
        Optional<OrderEntity> order = orderRepository.findByIdAndCustomer(orderId, customerEntity);
        if (order.isEmpty()) {
            throw new ValidationException("This order does not belong to you");
        }
        OrderStatusEntity cancelledStatus = orderStatusRepository.findByCode("CANCELLED")
                .orElseThrow(() -> new ValidationException("Cancelled order status not found"));

        List<OrderComboEntity> orderCombos = orderComboRepository.findByOrderDestination(order.get().getOrderDestination());
        try {
            for (OrderComboEntity combo : orderCombos) {
                combo.setCancelledReason(reason);
                combo.setReasonCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
                combo.setOrderStatus(cancelledStatus);
                orderComboRepository.save(combo);
            }
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }

    }

    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public void cancelOrderSeller(String orderComboId, String reason) {
        var account = authUtils.getUserAccountFromAuthentication();
        var seller = account.getSeller();
        if (seller == null) {
            throw new ValidationException("the account not role seller");
        }
        Optional<OrderComboEntity> optionalOrderCombo = orderComboRepository.findByIdAndSeller(orderComboId, seller);
        if (optionalOrderCombo.isEmpty()) {
            throw new ValidationException(" There is not your order combo");
        }
        OrderStatusEntity cancelledStatus = orderStatusRepository.findByCode("CANCELLED")
                .orElseThrow(() -> new ValidationException("Cancelled order status not found"));

        optionalOrderCombo.get().setCancelledReason(reason);
        optionalOrderCombo.get().setOrderStatus(cancelledStatus);
        optionalOrderCombo.get().setReasonCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        try {
            orderComboRepository.save(optionalOrderCombo.get());
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
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

    @Override
    public List<OrderResponse> getOrdersByCurrentCustomer() {
        CustomerEntity customer = getCurrentCustomerAccount();

        List<OrderEntity> orders = orderRepository.findByCustomer(customer);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationWrapper<List<TopSellersResponse>> getTopSellers(QueryWrapper queryWrapper) {
        return orderRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null) {
                query.distinct(true);
            }
            Join<OrderEntity, OrderItemEntity> itemJoin = root.join("orderItems");
            Join<OrderItemEntity, OrderComboEntity> comboJoin = itemJoin.join("orderCombo");
            Join<OrderComboEntity, OrderStatusEntity> statusJoin = comboJoin.join("orderStatus");

            predicates.add(criteriaBuilder.equal(statusJoin.get("code"), "PAYMENT_CONFIRMATION"));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, (items) -> {
            Map<String, TopSellersResponse.TopSellersResponseBuilder> sellerMap = buildSellerMap(items);

            List<TopSellersResponse> list = sellerMap.values().stream()
                    .map(TopSellersResponse.TopSellersResponseBuilder::build)
                    .sorted((a, b) -> Long.compare(b.getOrderCount(), a.getOrderCount()))
                    .collect(Collectors.toList());

            return new PaginationWrapper.Builder<List<TopSellersResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationWrapper<List<TopCustomerResponse>> getTopCustomerBySeller(QueryWrapper queryWrapper) {
        var account = authUtils.getUserAccountFromAuthentication();
        var seller = account.getSeller();
        if (seller == null) {
            throw new ValidationException("Seller profile not found");
        }

        return orderRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null) {
                query.distinct(true);
            }

            Join<OrderEntity, OrderItemEntity> itemJoin = root.join("orderItems", JoinType.LEFT);
            Join<OrderItemEntity, OrderComboEntity> comboJoin = itemJoin.join("orderCombo", JoinType.LEFT);
            Join<OrderComboEntity, SellerEntity> sellerJoin = comboJoin.join("seller", JoinType.LEFT);
            Join<OrderComboEntity, OrderStatusEntity> statusJoin = comboJoin.join("orderStatus", JoinType.LEFT);

            predicates.add(criteriaBuilder.equal(sellerJoin.get("id"), seller.getId()));

            predicates.add(criteriaBuilder.equal(statusJoin.get("code"), "PAYMENT_CONFIRMATION"));

            Map<String, QueryFieldWrapper> searchParams = queryWrapper.search();
            QueryFieldWrapper search = searchParams != null ? searchParams.get("keyword") : null;
            if (search != null && search.getValue() != null && !search.getValue().toString().trim().isEmpty()) {
                String searchPattern = "%" + search.getValue().toString().toLowerCase() + "%";
                Predicate customerNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("customer").get("name")), searchPattern);
                Predicate customerEmailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("customer").get("email")), searchPattern);
                predicates.add(criteriaBuilder.or(customerNamePredicate, customerEmailPredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, (items) -> {
            List<TopCustomerResponse> list = buildCustomerMap(items);

            return new PaginationWrapper.Builder<List<TopCustomerResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationWrapper<List<SellerOrderComboResponse>> getAllOrderCombosBySeller(QueryWrapper queryWrapper, String orderStatus) {
        var account = authUtils.getUserAccountFromAuthentication();
        var seller = account.getSeller();
        if (seller == null) {
            throw new ValidationException("User is not a seller");
        }
        var search = queryWrapper.search().toString();
        QueryFieldWrapper keyword = queryWrapper.search().remove("keyword");

        return orderComboRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();


            predicates.add(criteriaBuilder.equal(root.get("seller"), seller));
            if (orderStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("orderStatus").get("code"), orderStatus));
            } else {
                predicates.add(criteriaBuilder.not(root.get("orderStatus").get("code")).in(
                        OrderStatusCodes.PENDING, OrderStatusCodes.PAYMENT_CANCELLED, OrderStatusCodes.PAYMENT_FAILED
                ));
            }

            if (keyword != null && !keyword.getValue().toString().trim().isEmpty()) {
                String searchPattern = "%" + keyword.getValue().toString().toLowerCase() + "%";
                Join<OrderComboEntity, OrderDestinationEntity> destinationJoin = root.join("orderDestination", JoinType.LEFT);
                Join<OrderComboEntity, OrderItemEntity> itemJoin = root.joinSet("orderItems", JoinType.LEFT);
                Predicate customerNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(destinationJoin.get("customerName")), searchPattern
                );
                Predicate productNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(itemJoin.get("productName")), searchPattern
                );
                Predicate noItemsPredicate = criteriaBuilder.isEmpty(root.get("orderItems"));

                predicates.add(criteriaBuilder.or(
                        customerNamePredicate,
                        productNamePredicate,
                        noItemsPredicate
                ));
            }
            if (query != null) {
                query.distinct(true);
            }
            return getOrderComboPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::wrapSellerOrderComboResponse).stream().toList();
            return new PaginationWrapper.Builder<List<SellerOrderComboResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    @Override
    public PaginationWrapper<List<OrderStatusResponse>> getOrderStatusesTemplate(QueryWrapper queryWrapper) {
        return orderStatusRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            return getOrderStatusPredicate(param, root, criteriaBuilder, predicates);
        }, (items) -> {
            var list = items.map(this::wrapOrderStatusResponse).stream().toList();
            return new PaginationWrapper.Builder<List<OrderStatusResponse>>()
                    .setPaginationInfo(items)
                    .setData(list)
                    .build();
        });
    }

    private OrderStatusResponse wrapOrderStatusResponse(OrderStatusEntity orderStatus) {
        return OrderStatusResponse.builder()
                .id(orderStatus.getId())
                .code(orderStatus.getCode())
                .name(orderStatus.getName())
                .enabled(orderStatus.getEnabled())
                .build();
    }

    private OrderComboEntity getOrderComboById(String comboId) {
        return orderComboRepository.findById(comboId)
                .orElseThrow(() -> new ValidationException("Order combo not found with ID: " + comboId));
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

    private BigDecimal calculateSubtotal(List<ProductEntity> products, List<OrderItemRequest> items) {
        return products.stream()
                .map(product -> {
                    var quantity = items.stream().filter(item -> item.getProductId().equals(product.getId())).findFirst().orElseThrow(() -> new ValidationException("Product " + product.getId() + " not found in order items")).getQuantity();
                    return product.getCurrentPrice().multiply(BigDecimal.valueOf(quantity));
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
                                  List<OrderComboEntity> orderCombos, List<OrderItemRequest> requestItems) {
        Map<SellerEntity, OrderComboEntity> sellerComboMap = orderCombos.stream()
                .collect(Collectors.toMap(OrderComboEntity::getSeller, combo -> combo));
        Map<String, Integer> requestItemMap = requestItems.stream()
                .collect(Collectors.toMap(OrderItemRequest::getProductId, OrderItemRequest::getQuantity));
        for (ProductEntity product : products) {
            OrderComboEntity combo = sellerComboMap.get(product.getSeller());

            var quantity = requestItemMap.get(product.getId());
            if (quantity == null) {
                throw new ValidationException("Ordered quantity not found for product: " + product.getId());
            }
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(order)
                    .product(product)
                    .orderCombo(combo)
                    .productName(product.getName())
                    .shortDescription(product.getShortDescription())
                    .backgroundUrl(product.getThumbnail())
                    .basePrice(product.getCurrentPrice())
                    .quantity(quantity)
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
                .createDate(combo.getCreatedDate().toLocalDateTime())
                .build();
    }

    private SellerOrderComboResponse wrapSellerOrderComboResponse(OrderComboEntity combo) {
        var orderItems = combo.getOrderItems();
        var paymentStatus = orderStatusValidator.isPaymentCode(combo.getOrderStatus().getCode());
        var seller = combo.getSeller();
        var orderStatus = OrderStatusResponse.builder()
                .id(combo.getOrderStatus().getId())
                .code(combo.getOrderStatus().getCode())
                .name(combo.getOrderStatus().getName())
                .enabled(combo.getOrderStatus().getEnabled())
                .build();

        var orderDestination = combo.getOrderDestination();
        var orderDestinationResponse = wrapOrderDestinationResponse(orderDestination);
        var orderItemResponses = wrapCustomerOrderItemResponse(orderItems);
        return SellerOrderComboResponse.builder()
                .comboId(combo.getId())
                .sellerId(seller.getId())
                .sellerAvatarUrl(seller.getAvatarUrl())
                .sellerName(seller.getShopName())
                .grandPrice(combo.getGrandPrice())
                .orderStatusId(orderStatus.getId())
                .orderStatus(orderStatus)
                .items(orderItemResponses)
                .destination(orderDestinationResponse)
                .createDate(combo.getCreatedDate().toLocalDateTime())
                .updateDate(combo.getUpdatedDate().toLocalDateTime())
                .paymentStatus(paymentStatus)
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

    private Predicate getOrderStatusPredicate(Map<String, QueryFieldWrapper> param, Root<OrderStatusEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (param != null && !param.isEmpty()) {
            Predicate[] defaultPredicates = orderStatusRepository.createDefaultPredicate(criteriaBuilder, root, param);
            predicates.addAll(Arrays.asList(defaultPredicates));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private Map<String, TopSellersResponse.TopSellersResponseBuilder> buildSellerMap(Page<OrderEntity> items) {
        Map<String, TopSellersResponse.TopSellersResponseBuilder> sellerMap = new HashMap<>();

        items.getContent().forEach(order -> {
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                OrderComboEntity orderCombo = order.getOrderItems().iterator().next().getOrderCombo();
                if (orderCombo != null && orderCombo.getSeller() != null) {
                    String sellerId = orderCombo.getSeller().getId();
                    String sellerName = orderCombo.getSeller().getShopName();

                    sellerMap.computeIfAbsent(sellerId, k ->
                            TopSellersResponse.builder()
                                    .id(sellerId)
                                    .name(sellerName)
                                    .orderCount(0L)
                    );

                    TopSellersResponse.TopSellersResponseBuilder builder = sellerMap.get(sellerId);
                    TopSellersResponse current = builder.build();
                    builder.orderCount(current.getOrderCount() + 1);
                }
            }
        });

        return sellerMap;
    }

    private List<TopCustomerResponse> buildCustomerMap(Page<OrderEntity> items) {
        Map<String, TopCustomerResponse.TopCustomerResponseBuilder> customerMap = new HashMap<>();

        items.getContent().forEach(order -> {
            if (order.getCustomer() != null && order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                String customerId = order.getCustomer().getId();
                String customerName = order.getCustomer().getLastName();

                customerMap.computeIfAbsent(customerId, k ->
                        TopCustomerResponse.builder()
                                .customerId(customerId)
                                .customerName(customerName)
                                .orderCount(0L)
                );

                TopCustomerResponse.TopCustomerResponseBuilder builder = customerMap.get(customerId);
                TopCustomerResponse current = builder.build();
                builder.orderCount(current.getOrderCount() + 1);
            }
        });

        return customerMap.values().stream()
                .map(TopCustomerResponse.TopCustomerResponseBuilder::build)
                .sorted((a, b) -> Long.compare(b.getOrderCount(), a.getOrderCount()))
                .collect(Collectors.toList());
    }

    private void subtractProductQuantities(List<ProductEntity> products, List<OrderItemRequest> items) {
        Map<String, Integer> orderedQuantities = items.stream()
                .collect(Collectors.toMap(OrderItemRequest::getProductId, OrderItemRequest::getQuantity));
        for (ProductEntity product : products) {
            Integer orderedQuantity = orderedQuantities.get(product.getId());
            if (orderedQuantity == null) {
                throw new ValidationException("Product ID not found in request items: " + product.getId());
            }
            if (product.getQuantity() < orderedQuantity) {
                throw new ValidationException("Insufficient stock for product: " + product.getName());
            }
            product.setQuantity(product.getQuantity() - orderedQuantity);
        }
        productRepository.saveAll(products);
    }
}
