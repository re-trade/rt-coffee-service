package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.DeliveryTypeEnum;
import org.retrade.main.model.constant.NotificationTypeCode;
import org.retrade.main.model.constant.OrderStatusCodes;
import org.retrade.main.model.dto.request.CreateOrderHistoryRequest;
import org.retrade.main.model.dto.response.OrderHistoryResponse;
import org.retrade.main.model.dto.response.OrderStatusResponse;
import org.retrade.main.model.entity.*;
import org.retrade.main.model.message.SocketNotificationMessage;
import org.retrade.main.repository.jpa.*;
import org.retrade.main.service.MessageProducerService;
import org.retrade.main.service.OrderHistoryService;
import org.retrade.main.util.AuthUtils;
import org.retrade.main.validator.OrderStatusValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderHistoryServiceImpl implements OrderHistoryService {
    private final OrderHistoryRepository  orderHistoryRepository;
    private final OrderComboRepository orderComboRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderStatusValidator orderStatusValidator;
    private final AuthUtils authUtils;
    private final AccountRepository accountRepository;
    private final MessageProducerService messageProducerService;
    private final WalletTransactionRepository walletTransactionRepository;
    private final OrderComboDeliveryRepository orderComboDeliveryRepository;
    private final ProductRepository productRepository;

    @Override
    public List<OrderHistoryResponse> getAllNotesByOrderComboId(String id) {
        List<OrderHistoryEntity> orderHistoryEntityList = orderHistoryRepository.findByOrderCombo_Id(id);
        return orderHistoryEntityList.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderHistoryResponse getDetailsOrderHistory(String id) {
        OrderHistoryEntity orderHistoryEntity = orderHistoryRepository.findById(id).orElseThrow(
                ()-> new ValidationException("Không tìm thấy lịch sử đơn hàng")
        );
        return mapEntityToResponse(orderHistoryEntity);
    }

    @Transactional(rollbackFor = {ValidationException.class, ActionFailedException.class})
    @Override
    public OrderHistoryResponse createOrderHistory(CreateOrderHistoryRequest request) {
        var seller = getSellerEntity();
        if (seller == null) {
            throw new ValidationException("Không tìm thấy người bán");
        }

        var orderCombo = orderComboRepository.findByIdAndSeller(request.getOrderComboId(), seller)
                .orElseThrow(() -> new ValidationException("Đơn hàng này không thuộc về bạn"));

        OrderStatusEntity orderNewStatus = orderStatusRepository.findById(request.getNewStatusId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy trạng thái đơn hàng"));

        if (orderNewStatus.equals(orderCombo.getOrderStatus())) {
            throw new ValidationException("Trạng thái đơn hàng hiện tại đã là trạng thái này");
        }

        String currentStatusCode = orderCombo.getOrderStatus().getCode();
        String newStatusCode = orderNewStatus.getCode();

        if (!orderStatusValidator.isValidStatusTransition(currentStatusCode, newStatusCode)) {
            Set<String> validNextStatuses = orderStatusValidator.getValidNextStatuses(currentStatusCode);
            String validStatusesStr = String.join(", ", validNextStatuses);
            throw new ValidationException(
                    String.format("Không thể chuyển trạng thái từ %s sang %s. Các trạng thái hợp lệ tiếp theo: [%s]",
                            currentStatusCode, newStatusCode, validStatusesStr)
            );
        }

        OrderHistoryEntity orderHistoryEntity = new OrderHistoryEntity();
        orderHistoryEntity.setOrderCombo(orderCombo);
        orderHistoryEntity.setSeller(seller);
        orderHistoryEntity.setNotes(request.getNotes());
        orderHistoryEntity.setNewOrderStatus(orderNewStatus);
        orderHistoryEntity.setOldOrderStatus(orderCombo.getOrderStatus());
        orderHistoryEntity.setStatus(true);

        orderCombo.setOrderStatus(orderNewStatus);

        if (newStatusCode.equals(OrderStatusCodes.DELIVERING)) {
            if (request.getDeliveryType() == null) {
                throw new ValidationException("Vui lòng chọn hình thức giao hàng");
            }
            if (request.getDeliveryType() != DeliveryTypeEnum.MANUAL && request.getDeliveryCode() == null) {
                throw new ValidationException("Vui lòng nhập mã vận đơn");
            }
            var orderDelivery = OrderComboDeliveryEntity.builder()
                    .orderCombo(orderCombo)
                    .deliveryType(request.getDeliveryType())
                    .deliveryCode(request.getDeliveryCode() != null ? request.getDeliveryCode() : "")
                    .build();
            try {
                orderComboDeliveryRepository.save(orderDelivery);
            } catch (Exception e) {
                throw new ActionFailedException("Lỗi khi lưu thông tin vận chuyển đơn hàng", e);
            }
        } else if (newStatusCode.equals(OrderStatusCodes.DELIVERED)) {
            if (request.getDeliveryEvidenceImages() == null || request.getDeliveryEvidenceImages().isEmpty()) {
                throw new ValidationException("Vui lòng cung cấp ảnh chứng minh giao hàng");
            }
            orderCombo.setDeliveryCaptureImages(request.getDeliveryEvidenceImages());
        }

        try {
            orderComboRepository.save(orderCombo);
            orderHistoryRepository.save(orderHistoryEntity);
        } catch (Exception e) {
            throw new ActionFailedException("Lỗi khi lưu đơn hàng", e);
        }

        var accountCombo = orderCombo.getOrderDestination().getOrder().getCustomer().getAccount();
        BigDecimal rollbackPrice = BigDecimal.ZERO;

        if (newStatusCode.equals(OrderStatusCodes.CANCELLED)) {
            rollbackPrice = orderCombo.getGrandPrice();
            BigDecimal currentBalance = accountCombo.getBalance() != null ? accountCombo.getBalance() : BigDecimal.ZERO;
            accountCombo.setBalance(currentBalance.add(rollbackPrice));

            var walletTransactionHistory = WalletTransactionEntity.builder()
                    .note("Hoàn tiền đơn hàng " + orderCombo.getId() + ": " + rollbackPrice.toPlainString())
                    .amount(rollbackPrice)
                    .account(accountCombo)
                    .build();
            List<ProductEntity> updatedProducts = orderCombo.getOrderItems().stream()
                    .map(item -> {
                        ProductEntity product = item.getProduct();
                        product.setQuantity(product.getQuantity() + item.getQuantity());
                        return product;
                    })
                    .toList();
            try {
                walletTransactionRepository.save(walletTransactionHistory);
                accountRepository.save(accountCombo);
                productRepository.saveAll(updatedProducts);
            } catch (Exception e) {
                throw new ActionFailedException("Lỗi khi xử lí giao dịch hoàn tiền cho khách hàng");
            }

        }
        sendOrderStatusNotification(accountCombo, orderCombo, newStatusCode, rollbackPrice);

        return mapEntityToResponse(orderHistoryEntity);
    }


    private void sendOrderStatusNotification(AccountEntity account, OrderComboEntity orderCombo, String newStatusCode, BigDecimal rollbackPrice) {
        try {
            String title;
            String content;
            String message;

            switch (newStatusCode) {
                case OrderStatusCodes.CANCELLED -> {
                    title = "Đơn hàng " + orderCombo.getId() + " đã bị hủy";
                    content = "Đơn hàng " + orderCombo.getId() + " đã bị hủy. Số tiền "
                            + rollbackPrice.toPlainString() + " đã được hoàn lại vào tài khoản của bạn. Vui lòng kiểm tra số dư.";
                    message = "Đơn hàng đã hủy, hoàn tiền " + rollbackPrice.toPlainString();
                }
                case OrderStatusCodes.DELIVERING -> {
                    title = "Đơn hàng " + orderCombo.getId() + " đang được giao";
                    content = "Đơn hàng " + orderCombo.getId() + " đang trên đường giao đến bạn. Vui lòng chuẩn bị nhận hàng.";
                    message = "Đơn hàng đang giao";
                }
                case OrderStatusCodes.DELIVERED -> {
                    title = "Đơn hàng " + orderCombo.getId() + " đã giao thành công";
                    content = "Đơn hàng " + orderCombo.getId() + " đã được giao thành công. Chúc bạn mua sắm vui vẻ!";
                    message = "Đơn hàng đã giao";
                }
                case OrderStatusCodes.RETURN_APPROVED -> {
                    title = "Yêu cầu trả hàng đã được chấp nhận";
                    content = "Yêu cầu trả hàng cho đơn " + orderCombo.getId() + " đã được chấp nhận. Vui lòng tiến hành gửi trả.";
                    message = "Trả hàng được chấp nhận";
                }
                case OrderStatusCodes.RETURN_REJECTED -> {
                    title = "Yêu cầu trả hàng bị từ chối";
                    content = "Yêu cầu trả hàng cho đơn " + orderCombo.getId() + " đã bị từ chối. Vui lòng liên hệ hỗ trợ để biết thêm chi tiết.";
                    message = "Trả hàng bị từ chối";
                }
                default -> {
                    title = "Trạng thái đơn hàng " + orderCombo.getId() + " đã thay đổi";
                    content = "Đơn hàng " + orderCombo.getId() + " hiện đang ở trạng thái " + newStatusCode.replace("_", " ").toLowerCase() + ".";
                    message = "Trạng thái đơn hàng thay đổi";
                }
            }

            messageProducerService.sendSocketNotification(SocketNotificationMessage.builder()
                    .accountId(account.getId())
                    .messageId(UUID.randomUUID().toString())
                    .title(title)
                    .type(NotificationTypeCode.ORDER)
                    .content(content)
                    .message(message)
                    .build());

        } catch (Exception e) {
            log.error("Gửi thông báo thất bại cho đơn {} với trạng thái {}", orderCombo.getId(), newStatusCode, e);
        }
    }



    @Override
    public OrderHistoryResponse updateOrderHistory(String id) {
        var seller = getSellerEntity();
        OrderHistoryEntity orderHistoryEntity = orderHistoryRepository.findByIdAndSeller(id, seller);
        if (orderHistoryEntity == null) {
            throw new ValidationException("Order history not found");
        }
        orderHistoryEntity.setNotes(orderHistoryEntity.getNotes());
        orderHistoryEntity.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
        try {
            orderHistoryRepository.save(orderHistoryEntity);
            return mapEntityToResponse(orderHistoryEntity);
        }catch (Exception e) {
            throw new ActionFailedException(e.getMessage());
        }
    }

    private SellerEntity getSellerEntity() {
       return authUtils.getUserAccountFromAuthentication().getSeller();
    }
    private OrderHistoryResponse mapEntityToResponse(OrderHistoryEntity orderHistoryEntity) {
        return OrderHistoryResponse
                .builder()
                .id(orderHistoryEntity.getId())
                .orderComboId(orderHistoryEntity.getOrderCombo().getId())
                .sellerId(orderHistoryEntity.getSeller().getId())
                .notes(orderHistoryEntity.getNotes())
                .newOrderStatus(mapEntityToResponse(orderHistoryEntity.getNewOrderStatus()))
                .oldOrderStatus(mapEntityToResponse(orderHistoryEntity.getOldOrderStatus()))
                .orderComboId(orderHistoryEntity.getOrderCombo().getId())
                .createdAt(orderHistoryEntity.getCreatedDate().toLocalDateTime())
                .updatedAt(orderHistoryEntity.getUpdatedDate().toLocalDateTime())
                .build();
    }
    private OrderStatusResponse mapEntityToResponse(OrderStatusEntity orderStatusEntity) {
        return OrderStatusResponse
                .builder()
                .id(orderStatusEntity.getId())
                .code(orderStatusEntity.getCode())
                .name(orderStatusEntity.getName())
                .build();
    }

}
