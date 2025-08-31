package org.retrade.main.service.impl;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.constant.QueryOperatorEnum;
import org.retrade.common.model.dto.request.QueryFieldWrapper;
import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.common.model.dto.response.PaginationWrapper;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.ConditionTypeCode;
import org.retrade.main.model.constant.NotificationTypeCode;
import org.retrade.main.model.constant.OrderStatusCodes;
import org.retrade.main.model.dto.request.CancelOrderRequest;
import org.retrade.main.model.dto.request.CreateOrderRequest;
import org.retrade.main.model.dto.request.OrderItemRequest;
import org.retrade.main.model.dto.response.*;
import org.retrade.main.model.entity.*;
import org.retrade.main.model.message.AchievementMessage;
import org.retrade.main.model.message.SocketNotificationMessage;
import org.retrade.main.repository.jpa.*;
import org.retrade.main.service.MessageProducerService;
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
    private final AuthUtils authUtils;
    private final OrderStatusValidator  orderStatusValidator;
    private final PlatformFeeTierRepository platformFeeTierRepository;
    private final SellerRevenueRepository sellerRevenueRepository;
    private final AccountRepository accountRepository;
    private final MessageProducerService messageProducerService;

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
        BigDecimal discountTotal = BigDecimal.ZERO;

        BigDecimal grandTotal = subtotal.subtract(discountTotal);

        OrderEntity order = createOrderEntity(customer, subtotal, BigDecimal.ZERO, discountTotal, grandTotal);
        OrderEntity savedOrder;
        OrderDestinationEntity orderDestination;
        try {
            savedOrder = orderRepository.save(order);
            orderDestinationEntity.setOrder(savedOrder);
            orderDestination = orderDestinationRepository.save(orderDestinationEntity);
        } catch (Exception e) {
            throw new ActionFailedException("Lưu thông tin địa chỉ giao hàng thất bại", e);
        }
        List<OrderComboEntity> orderCombos = createOrderCombos(productsBySeller, orderDestination, request.getItems());

        createOrderItems(savedOrder, products, orderCombos, request.getItems());

        return mapToOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(String orderId) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getCustomer() == null) {
            throw new ValidationException("Người dùng không phải là khách hàng");
        }
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy đơn hàng với ID: " + orderId));
        if (!order.getCustomer().getId().equals(account.getCustomer().getId())) {
            throw new ValidationException("Bạn không phải là chủ sở hữu của tài khoản này");
        }
        return mapToOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByCustomer(String customerId) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy khách hàng với ID: " + customerId));

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
            throw new ValidationException("Người dùng không phải là khách hàng");
        }
        return orderComboRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null) {
                query.distinct(true);
            }
            var destinationJoin = root.join("orderDestination", JoinType.INNER);
            var orderJoin = destinationJoin.join("order", JoinType.INNER);
            var customerJoin = orderJoin.join("customer", JoinType.INNER);

            predicates.add(criteriaBuilder.equal(customerJoin.get("id"), customerEntity.getId()));
            return getCustomerOrderComboPredicate(param, root, criteriaBuilder, predicates);
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
                .orElseThrow(() -> new ValidationException("Không tìm thấy đơn hàng với ID: " + orderId));
        var customerEntity = order.getCustomer();
        OrderStatusEntity newStatus = orderStatusRepository.findByCode(statusCode)
                .orElseThrow(() -> new ValidationException("Không tìm thấy trạng thái đơn hàng: " + statusCode));

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
                .orElseThrow(() -> new ValidationException("Không tìm thấy đơn hàng với ID: " + orderId));
        var customerEntity = order.getCustomer();
        OrderStatusEntity cancelledStatus = orderStatusRepository.findByCode("CANCELLED")
                .orElseThrow(() -> new ValidationException("Không tìm thấy trạng thái đơn hàng đã hủy"));

        List<OrderComboEntity> orderCombos = orderComboRepository.findByOrderDestination(order.getOrderDestination());
        for (OrderComboEntity combo : orderCombos) {
            combo.setOrderStatus(cancelledStatus);
            orderComboRepository.save(combo);
        }
    }

    @Override
    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    public void cancelOrderCustomer(CancelOrderRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getCustomer() == null) {
            throw new ValidationException("Tài khoản của bạn không thuộc loại khách hàng");
        }
        var customerEntity = account.getCustomer();
        var validStatus = Set.of(OrderStatusCodes.PENDING, OrderStatusCodes.PAYMENT_CONFIRMATION);
        var orderComboEntity = orderComboRepository.findById(request.orderComboId()).orElseThrow(() -> new ValidationException("Không tìm thấy đơn hàng với ID: " + request.orderComboId() + " for customer: " + customerEntity.getId()));
        if (!validStatus.contains(orderComboEntity.getOrderStatus().getCode())) {
            throw new ValidationException("Trạng thái của đơn hàng không hợp lệ để hủy: " + orderComboEntity.getOrderStatus().getCode());
        }
        if (!orderComboEntity.getOrderDestination().getOrder().getCustomer().getId().equals(customerEntity.getId())) {
            throw new ValidationException("Bạn không phải là chủ sở hữu");
        }
        OrderStatusEntity cancelledStatus = orderStatusRepository.findByCode(OrderStatusCodes.CANCELLED)
                .orElseThrow(() -> new ValidationException("Không tìm thấy trạng thái đơn hàng đã hủy"));
        orderComboEntity.setCancelledReason(request.reason());
        orderComboEntity.setOrderStatus(cancelledStatus);
        orderComboEntity.setReasonCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        List<ProductEntity> updatedProducts = orderComboEntity.getOrderItems().stream()
                .map(item -> {
                    ProductEntity product = item.getProduct();
                    product.setQuantity(product.getQuantity() + item.getQuantity());
                    return product;
                })
                .collect(Collectors.toList());
        try {
            orderComboRepository.save(orderComboEntity);
            productRepository.saveAll(updatedProducts);
            if (orderStatusValidator.isPaymentSuccessful(orderComboEntity.getOrderStatus().getCode())) {
                BigDecimal rollbackPrice = orderComboEntity.getGrandPrice();
                BigDecimal currentBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
                account.setBalance(currentBalance.add(rollbackPrice));
                accountRepository.save(account);
            }
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class})
    @Override
    public void cancelOrderSeller(CancelOrderRequest request) {
        var account = authUtils.getUserAccountFromAuthentication();
        var seller = account.getSeller();
        if (seller == null) {
            throw new ValidationException("Tài khoản không phải là người bán");
        }
        var orderCombo = orderComboRepository.findByIdAndSeller(request.orderComboId(), seller).orElseThrow(() -> new ValidationException("There is not your order combo"));
        OrderStatusEntity cancelledStatus = orderStatusRepository.findByCode(OrderStatusCodes.CANCELLED)
                .orElseThrow(() -> new ValidationException("Không tìm thấy trạng thái đơn hàng đã hủy"));

        orderCombo.setCancelledReason(request.reason());
        orderCombo.setOrderStatus(cancelledStatus);
        orderCombo.setReasonCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        var accountCombo = orderCombo.getOrderDestination().getOrder().getCustomer().getAccount();
        BigDecimal rollbackPrice = orderCombo.getGrandPrice();
        BigDecimal currentBalance = accountCombo.getBalance() != null ? accountCombo.getBalance() : BigDecimal.ZERO;
        accountCombo.setBalance(currentBalance.add(rollbackPrice));
        List<ProductEntity> updatedProducts = orderCombo.getOrderItems().stream()
                .map(item -> {
                    ProductEntity product = item.getProduct();
                    product.setQuantity(product.getQuantity() + item.getQuantity());
                    return product;
                })
                .toList();
        try {
            orderComboRepository.save(orderCombo);
            productRepository.saveAll(updatedProducts);
            accountRepository.save(accountCombo);
        } catch (Exception e) {
            throw new ActionFailedException(e.getMessage());
        }
    }

    @Transactional(rollbackFor = {ActionFailedException.class, Exception.class, ValidationException.class})
    @Override
    public void confirmDelivery(String id) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getCustomer() == null) {
            throw new ValidationException("Tài khoản không phải là khách hàng");
        }
        var customerEntity = account.getCustomer();
        var orderComboEntity = getOrderComboFromCustomerById(id, customerEntity);
        if (!OrderStatusCodes.DELIVERED.equals(orderComboEntity.getOrderStatus().getCode())) {
            throw new ValidationException(
                    "Trạng thái của đơn hàng không hợp lệ để xác nhận giao hàng: " + orderComboEntity.getOrderStatus().getCode()
            );
        }
        OrderStatusEntity completedStatus = orderStatusRepository.findByCode(OrderStatusCodes.RETRIEVED)
                .orElseThrow(() -> new ValidationException("Không tìm thấy trạng thái đơn hàng đã hoàn tất"));
        orderComboEntity.setOrderStatus(completedStatus);
        try {
            orderComboRepository.save(orderComboEntity);
        } catch (Exception e) {
            throw new ActionFailedException(e.getMessage());
        }
        var sellerAccount = orderComboEntity.getSeller().getAccount();
        sendOrderStatusNotification(account, orderComboEntity, OrderStatusCodes.RETRIEVED, false);
        sendOrderStatusNotification(sellerAccount, orderComboEntity, OrderStatusCodes.RETRIEVED, true);
    }

    @Transactional(rollbackFor = {ActionFailedException.class, ValidationException.class, Exception.class})
    @Override
    public void completedOrder(String id) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getCustomer() == null) {
            throw new ValidationException("Tài khoản không phải là khách hàng");
        }
        var customerEntity = account.getCustomer();
        var orderComboEntity = getOrderComboFromCustomerById(id, customerEntity);
        if (!OrderStatusCodes.RETRIEVED.equals(orderComboEntity.getOrderStatus().getCode())) {
            throw new ValidationException(
                    "Trạng thái của đơn hàng không hợp lệ để hoàn tất: " + orderComboEntity.getOrderStatus().getCode()
            );
        }
        OrderStatusEntity completedStatus = orderStatusRepository.findByCode(OrderStatusCodes.COMPLETED)
                .orElseThrow(() -> new ValidationException("Không tìm thấy trạng thái đơn hàng đã hoàn tất"));
        orderComboEntity.setOrderStatus(completedStatus);
        BigDecimal totalAmount = orderComboEntity.getGrandPrice();
        PlatformFeeTierEntity tier = platformFeeTierRepository.findMatchingTier(totalAmount)
                .orElseThrow(() -> new ValidationException("No fee tier found for amount: " + totalAmount));
        BigDecimal feeAmount = totalAmount.multiply(tier.getFeeRate())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal sellerRevenueAmount = totalAmount.subtract(feeAmount);
        var sellerAccount = orderComboEntity.getSeller().getAccount();
        BigDecimal currentBalance = sellerAccount.getBalance() != null ? sellerAccount.getBalance() : BigDecimal.ZERO;
        sellerAccount.setBalance(currentBalance.add(sellerRevenueAmount));
        SellerRevenueEntity revenueEntity = SellerRevenueEntity.builder()
                .orderCombo(orderComboEntity)
                .totalAmount(totalAmount)
                .platformFeeRate(tier.getFeeRate().doubleValue())
                .platformFeeAmount(feeAmount)
                .sellerRevenue(sellerRevenueAmount)
                .build();
        try {
            orderComboRepository.save(orderComboEntity);
            accountRepository.save(sellerAccount);
            sellerRevenueRepository.save(revenueEntity);
        } catch (Exception e) {
            throw new ActionFailedException(e.getMessage());
        }
        sendOrderStatusNotification(account, orderComboEntity, OrderStatusCodes.COMPLETED, false);
        sendOrderStatusNotification(sellerAccount, orderComboEntity, OrderStatusCodes.COMPLETED, true);
        sendAchievementMessage(orderComboEntity.getSeller());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationWrapper<List<CustomerOrderComboResponse>> getSellerOrderCombos(QueryWrapper queryFieldWrapper) {
        var account = authUtils.getUserAccountFromAuthentication();
        var seller = account.getSeller();
        if (seller == null) {
            throw new ValidationException("Tài khoản không phải là người bán");
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
            throw new ValidationException("Tài khoản không phải là người bán");
        }
        var orderSeller = combo.getSeller();
        if (!seller.getId().equals(orderSeller.getId())) {
            throw new ValidationException("Bạn không phải là chủ sở hữu tài khoản");
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
            throw new ValidationException("Tài khoản không phải là khách hàng");
        }
        var comboCustomer = combo.getOrderDestination().getOrder().getCustomer();
        if (!customer.getId().equals(comboCustomer.getId())) {
            throw new ValidationException("Bạn không phải là chủ sở hữu tài khoản");
        }
        return wrapCustomerOrderComboResponse(combo);
    }

    @Transactional(readOnly = true)
    @Override
    public SellerOrderComboResponse getAdminOrderComboById(String comboId) {
        var combo = getOrderComboById(comboId);
        return wrapSellerOrderComboResponse(combo);
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
            throw new ValidationException("Không tìm thấy hồ sơ người bán");
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

            predicates.add(criteriaBuilder.equal(statusJoin.get("code"), OrderStatusCodes.PAYMENT_CONFIRMATION));

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
            throw new ValidationException("Tài khoản không phải là khách hàng");
        }
        QueryFieldWrapper keyword = queryWrapper.search().remove("keyword");

        return orderComboRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            assert query != null;
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
            query.distinct(true);
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
    @Transactional(readOnly = true)
    public PaginationWrapper<List<SellerOrderComboResponse>> getAllOrderCombos(QueryWrapper queryWrapper, String orderStatus) {
        QueryFieldWrapper keyword = queryWrapper.search().remove("keyword");
        return orderComboRepository.query(queryWrapper, (param) -> (root, query, criteriaBuilder) -> {
            assert query != null;
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.getValue().toString().trim().isEmpty()) {
                String searchPattern = "%" + keyword.getValue().toString().toLowerCase() + "%";
                Join<OrderComboEntity, OrderDestinationEntity> destinationJoin = root.join("orderDestination", JoinType.LEFT);
                Join<OrderComboEntity, OrderItemEntity> itemJoin = root.joinSet("orderItems", JoinType.LEFT);
                Join<OrderComboEntity, SellerEntity> sellerJoin = root.join("seller", JoinType.INNER);
                Predicate sellerNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(sellerJoin.get("shopName")), searchPattern
                );
                Predicate customerNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(destinationJoin.get("customerName")), searchPattern
                );
                Predicate productNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(itemJoin.get("productName")), searchPattern
                );
                Predicate noItemsPredicate = criteriaBuilder.isEmpty(root.get("orderItems"));

                predicates.add(criteriaBuilder.or(
                        sellerNamePredicate,
                        customerNamePredicate,
                        productNamePredicate,
                        noItemsPredicate
                ));
            }
            query.distinct(true);
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
    public PaginationWrapper<List<CustomerOrderComboResponse>> getOrderComboCustomerCanReport(QueryWrapper queryWrapper) {
        var account = authUtils.getUserAccountFromAuthentication();
        if (account.getCustomer() == null) {
            throw new ValidationException("Tài khoản không phải là khách hàng");
        }
        var customer = account.getCustomer();
        return orderComboRepository.query(queryWrapper, (param) -> ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            var destinationJoin = root.join("orderDestination", JoinType.INNER);
            var orderJoin = destinationJoin.join("order", JoinType.INNER);
            predicates.add(criteriaBuilder.equal(orderJoin.get("customer"), customer));
            var sellerId = param.remove("sellerId");
            if (sellerId != null) {
                var value = sellerId.getValue().toString();
                predicates.add(criteriaBuilder.equal(root.get("seller").get("id"), value));
            }
            var allowedStatus = Set.of(
                    OrderStatusCodes.DELIVERED,
                    OrderStatusCodes.RETRIEVED,
                    OrderStatusCodes.COMPLETED,
                    OrderStatusCodes.RETURN_REQUESTED,
                    OrderStatusCodes.RETURN_APPROVED,
                    OrderStatusCodes.RETURN_REJECTED,
                    OrderStatusCodes.RETURNED
            );
            predicates.add(root.get("orderStatus").get("code").in(allowedStatus));
            return getCustomerOrderComboPredicate(param, root, criteriaBuilder, predicates);
        }), (items) -> {
            var list = items.map(this::wrapCustomerOrderComboResponse).stream().toList();
            return new PaginationWrapper.Builder<List<CustomerOrderComboResponse>>()
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

    @Override
    public OrderStatsResponse getStatsOrderCustomer() {
        var account = authUtils.getUserAccountFromAuthentication();
        var customerEntity = account.getCustomer();
        if (customerEntity == null) {
            throw new ValidationException("Tài khoản không phải là khách hàng");
        }

        List<OrderComboEntity> orderComboEntities = orderComboRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            var destinationJoin = root.join("orderDestination", JoinType.INNER);
            var orderJoin = destinationJoin.join("order", JoinType.INNER);
            var customerJoin = orderJoin.join("customer", JoinType.INNER);

            predicates.add(criteriaBuilder.equal(customerJoin.get("id"), customerEntity.getId()));
            return getOrderComboPredicate(new HashMap<>(), root, criteriaBuilder, predicates);
        });

        long totalOrders = orderComboEntities.size();
        long totalOrdersCompleted = orderComboEntities.stream()
                .filter(cb -> OrderStatusCodes.COMPLETED.equalsIgnoreCase(cb.getOrderStatus().getCode()))
                .count();
        long totalOrdersBeingDelivered = orderComboEntities.stream()
                .filter(cb -> OrderStatusCodes.DELIVERING.equalsIgnoreCase(cb.getOrderStatus().getCode()))
                .count();
        BigDecimal totalCost = orderComboEntities.stream()
                .filter(combo -> OrderStatusCodes.COMPLETED.equalsIgnoreCase(combo.getOrderStatus().getCode()))
                .map(OrderComboEntity::getGrandPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderStatsResponse.builder()
                .totalOrdersBeingDelivered(totalOrdersBeingDelivered)
                .totalOrders(totalOrders)
                .totalOrdersCompleted(totalOrdersCompleted)
                .totalPaymentCost(totalCost)
                .build();
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
                .orElseThrow(() -> new ValidationException("Không tìm thấy đơn hàng với ID: " + comboId));
    }

    private CustomerEntity getCurrentCustomerAccount() {
        var account = authUtils.getUserAccountFromAuthentication();
        var customerEntity = account.getCustomer();
        if (customerEntity == null) {
            throw new ValidationException("Người dùng không phải là khách hàng");
        }
        return customerEntity;
    }

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request.getItems().isEmpty()) {
            throw new ValidationException("Danh sách sản phẩm không được để trống");
        }

        if (request.getItems().size() > 100) {
            throw new ValidationException("Không thể đặt hơn 100 sản phẩm khác nhau trong một lần");
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
                    throw new ValidationException("Sản phẩm với ID: " + productId + " chưa được xác minh");
                }
                if (product.getQuantity() < item.getQuantity()) {
                    throw new ValidationException("Sản phẩm với ID: " + productId + " không đủ số lượng");
                }
                products.add(product);
            } else {
                notFoundProducts.add(productId);
            }
        }

        if (!notFoundProducts.isEmpty()) {
            throw new ValidationException("Không tìm thấy sản phẩm: " + String.join(", ", notFoundProducts));
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

    private List<OrderComboEntity> createOrderCombos(Map<SellerEntity, List<ProductEntity>> productsBySeller, OrderDestinationEntity orderDestination, List<OrderItemRequest> items) {
        OrderStatusEntity pendingStatus = orderStatusRepository.findByCode("PENDING")
                .orElseThrow(() -> new ValidationException("Không tìm thấy trạng thái đơn hàng đang chờ"));

        List<OrderComboEntity> orderCombos = new ArrayList<>();

        for (Map.Entry<SellerEntity, List<ProductEntity>> entry : productsBySeller.entrySet()) {
            SellerEntity seller = entry.getKey();
            List<ProductEntity> sellerProducts = entry.getValue();
            BigDecimal sellerTotal = calculateSubtotal(sellerProducts, items);
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
            throw new ActionFailedException("Không thể lưu thông tin gói đơn hàng", ex);
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
                throw new ValidationException("Không tìm thấy số lượng đặt cho sản phẩm: " + product.getId());
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
        String paymentStatus;
        if (Set.of(OrderStatusCodes.PENDING, OrderStatusCodes.PAYMENT_FAILED, OrderStatusCodes.PAYMENT_CANCELLED).contains(orderStatus.getCode())) {
            paymentStatus = "Not Paid";
        }else {
            paymentStatus = "Paid";
        }
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
                .updateDate(combo.getUpdatedDate().toLocalDateTime())
                .paymentStatus(paymentStatus)
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
                throw new ValidationException("Không tìm thấy sản phẩm với mã: " + product.getId());
            }
            if (product.getQuantity() < orderedQuantity) {
                throw new ValidationException("Không đủ tồn kho cho sản phẩm: " + product.getName());
            }
            product.setQuantity(product.getQuantity() - orderedQuantity);
        }
        productRepository.saveAll(products);
    }

    private void sendOrderStatusNotification(AccountEntity account, OrderComboEntity orderCombo, String newStatusCode, boolean isSeller) {
        var rollbackPrice = orderCombo.getGrandPrice();
        try {
            String title;
            String content;
            String message;

            switch (newStatusCode) {
                case OrderStatusCodes.CANCELLED -> {
                    if (isSeller) {
                        title = "Đơn hàng #" + orderCombo.getId() + " của khách đã bị hủy";
                        content = "Khách hàng đã hủy đơn. Vui lòng kiểm tra chi tiết trong hệ thống.";
                        message = "Khách hủy đơn hàng #" + orderCombo.getId();
                    } else {
                        title = "Đơn hàng #" + orderCombo.getId() + " đã bị hủy";
                        String refundNote = (rollbackPrice != null && rollbackPrice.compareTo(BigDecimal.ZERO) > 0)
                                ? " Số tiền " + rollbackPrice.toPlainString() + " VND sẽ/đã được hoàn về tài khoản của bạn."
                                : " Không phát sinh hoàn tiền.";
                        content = "Đơn hàng của bạn đã được hủy." + refundNote;
                        message = (rollbackPrice != null && rollbackPrice.compareTo(BigDecimal.ZERO) > 0)
                                ? "Đã hủy đơn, hoàn " + rollbackPrice.toPlainString() + " VND"
                                : "Đã hủy đơn";
                    }
                }
                case OrderStatusCodes.RETRIEVED -> {
                    if (isSeller) {
                        title = "Đơn hàng #" + orderCombo.getId() + " đã được khách nhận";
                        content = "Khách hàng đã xác nhận đã nhận đơn hàng. Vui lòng lưu lại để đối soát.";
                        message = "Khách đã nhận hàng";
                    } else {
                        title = "Đơn hàng #" + orderCombo.getId() + " đã được nhận";
                        content = "Bạn đã nhận hàng thành công. Vui lòng kiểm tra sản phẩm ngay sau khi nhận.";
                        message = "Đã nhận hàng";
                    }
                }
                case OrderStatusCodes.COMPLETED -> {
                    if (isSeller) {
                        title = "Đơn hàng #" + orderCombo.getId() + " đã hoàn tất";
                        content = "Đơn hàng đã hoàn tất. Doanh thu sẽ được cập nhật trong báo cáo.";
                        message = "Hoàn tất đơn, cập nhật doanh thu";
                    } else {
                        title = "Đơn hàng #" + orderCombo.getId() + " đã hoàn tất";
                        content = "Giao dịch đã hoàn tất. Cảm ơn bạn đã sử dụng dịch vụ!";
                        message = "Hoàn tất đơn";
                    }
                }
                default -> {
                    if (isSeller) {
                        title = "Đơn hàng #" + orderCombo.getId() + " thay đổi trạng thái";
                        content = "Đơn hàng hiện ở trạng thái: " + newStatusCode.replace("_", " ").toLowerCase() + ".";
                        message = "Trạng thái đơn hàng: " + newStatusCode;
                    } else {
                        title = "Cập nhật trạng thái đơn hàng #" + orderCombo.getId();
                        content = "Đơn hàng của bạn hiện ở trạng thái: " + newStatusCode.replace("_", " ").toLowerCase() + ".";
                        message = "Trạng thái: " + newStatusCode;
                    }
                }
            }

            messageProducerService.sendSocketNotification(
                    SocketNotificationMessage.builder()
                            .accountId(account.getId())
                            .messageId(UUID.randomUUID().toString())
                            .title(title)
                            .type(NotificationTypeCode.ORDER)
                            .content(content)
                            .message(message)
                            .build()
            );

        } catch (Exception e) {
            log.error("Gửi thông báo thất bại cho đơn {} với trạng thái {}", orderCombo.getId(), newStatusCode, e);
        }
    }

    private void sendAchievementMessage(SellerEntity seller) {
        try {
            messageProducerService.sendAchievementMessage(
                    AchievementMessage.builder()
                            .sellerId(seller.getId())
                            .eventType(ConditionTypeCode.ORDER_COMPLETED)
                            .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private OrderComboEntity getOrderComboFromCustomerById (String id, CustomerEntity customerEntity) {
        var orderComboEntity = orderComboRepository.findById(id)
                .orElseThrow(() -> new ValidationException(
                        "Không tìm thấy gói đơn hàng với ID: " + id + " cho khách hàng: " + customerEntity.getId()
                ));
        if (!orderComboEntity.getOrderDestination().getOrder().getCustomer().getId()
                .equals(customerEntity.getId())) {
            throw new ValidationException("Bạn không phải chủ sở hữu");
        }
        return orderComboEntity;
    }

    private Predicate getCustomerOrderComboPredicate(Map<String, QueryFieldWrapper> param, Root<OrderComboEntity> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        var keyword = param.remove("keyword");
        var orderStatusId = param.remove("orderStatusId");
        if (keyword != null && !keyword.getValue().toString().trim().isEmpty()) {
            var value = keyword.getValue().toString().trim().toLowerCase();
            var pattern = String.format("%%%s%%", value);
            var orderItemJoin = root.joinSet("orderItems", JoinType.LEFT);
            var sellerJoin = root.join("seller", JoinType.INNER);
            Predicate sellerNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(sellerJoin.get("shopName")), pattern
            );
            Predicate sellerDescriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(sellerJoin.get("description")), pattern
            );
            Predicate noItemsPredicate = criteriaBuilder.isEmpty(root.get("orderItems"));

            Predicate productNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(orderItemJoin.get("productName")), pattern
            );
            predicates.add(criteriaBuilder.or(
                    productNamePredicate,
                    sellerNamePredicate,
                    sellerDescriptionPredicate,
                    noItemsPredicate
            ));
        }
        if (orderStatusId != null) {
            var orderStatusJoin = root.join("orderStatus", JoinType.INNER);
            switch (orderStatusId.getOperator()) {
                case QueryOperatorEnum.EQ -> {
                    predicates.add(criteriaBuilder.equal(orderStatusJoin.get("id"), orderStatusId.getValue()));
                }
                case QueryOperatorEnum.IN -> {
                    Object value = orderStatusId.getValue();
                    if (value instanceof List<?>) {
                        var idList = ((List<?>) value).stream().map(Object::toString).toList();
                        predicates.add(orderStatusJoin.get("id").in(idList));
                    }
                }
            }
        }
        return getOrderComboPredicate(param, root, criteriaBuilder, predicates);
    }


}
