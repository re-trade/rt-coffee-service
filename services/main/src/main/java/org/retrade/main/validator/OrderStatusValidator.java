package org.retrade.main.validator;

import lombok.RequiredArgsConstructor;
import org.retrade.main.model.constant.OrderStatusCodes;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OrderStatusValidator {

    // Định nghĩa thứ tự của các status
    private static final Map<String, Integer> STATUS_ORDER = new HashMap<>();

    static {
        STATUS_ORDER.put(OrderStatusCodes.PENDING, 1);        // Đơn hàng chờ thanh toán
        STATUS_ORDER.put(OrderStatusCodes.PAYMENT_FAILED, 2); // Thanh toán thất bại
        STATUS_ORDER.put(OrderStatusCodes.PAYMENT_CANCELLED, 3); // Thanh toán bị hủy
        STATUS_ORDER.put(OrderStatusCodes.PAYMENT_CONFIRMATION, 4); // Đã thanh toán, chờ xác nhận
        STATUS_ORDER.put(OrderStatusCodes.PREPARING, 5);      // Đang chuẩn bị hàng
        STATUS_ORDER.put(OrderStatusCodes.DELIVERING, 6);     // Đang giao hàng
        STATUS_ORDER.put(OrderStatusCodes.DELIVERED, 7);      // Đã giao hàng
        STATUS_ORDER.put(OrderStatusCodes.COMPLETED, 8);      // Hoàn thành
        STATUS_ORDER.put(OrderStatusCodes.RETURN_REQUESTED, 9);
        STATUS_ORDER.put(OrderStatusCodes.RETURN_APPROVED, 10);
        STATUS_ORDER.put(OrderStatusCodes.RETURNING, 11);
        STATUS_ORDER.put(OrderStatusCodes.RETURNED, 12);
        STATUS_ORDER.put(OrderStatusCodes.RETURN_REJECTED, 13);
        STATUS_ORDER.put(OrderStatusCodes.REFUNDED, 14);
        STATUS_ORDER.put(OrderStatusCodes.CANCELLED, 15);
    }

    // Định nghĩa các trạng thái có thể chuyển đổi hợp lệ
    private static final Map<String, Set<String>> VALID_TRANSITIONS = new HashMap<>();

    static {
        VALID_TRANSITIONS.put(OrderStatusCodes.PENDING, Set.of(
                OrderStatusCodes.PAYMENT_FAILED,
                OrderStatusCodes.PAYMENT_CANCELLED,
                OrderStatusCodes.PAYMENT_CONFIRMATION,
                OrderStatusCodes.CANCELLED
        ));
        VALID_TRANSITIONS.put(OrderStatusCodes.PAYMENT_FAILED, Set.of(
                OrderStatusCodes.PAYMENT_CONFIRMATION,
                OrderStatusCodes.CANCELLED
        ));
        VALID_TRANSITIONS.put(OrderStatusCodes.PAYMENT_CANCELLED, Set.of(
                OrderStatusCodes.CANCELLED
        ));
        VALID_TRANSITIONS.put(OrderStatusCodes.PAYMENT_CONFIRMATION, Set.of(
                OrderStatusCodes.PREPARING,
                OrderStatusCodes.CANCELLED
        ));
        VALID_TRANSITIONS.put(OrderStatusCodes.PREPARING, Set.of(
                OrderStatusCodes.DELIVERING,
                OrderStatusCodes.CANCELLED
        ));
        VALID_TRANSITIONS.put(OrderStatusCodes.DELIVERING, Set.of(
                OrderStatusCodes.DELIVERED,
                OrderStatusCodes.CANCELLED
        ));
        VALID_TRANSITIONS.put(OrderStatusCodes.DELIVERED, Set.of(
                OrderStatusCodes.COMPLETED,
                OrderStatusCodes.RETURN_REQUESTED
        ));
        VALID_TRANSITIONS.put(OrderStatusCodes.COMPLETED, Set.of(
                OrderStatusCodes.RETURN_REQUESTED
        ));
        VALID_TRANSITIONS.put(OrderStatusCodes.RETURN_REQUESTED, Set.of(
                OrderStatusCodes.RETURN_APPROVED,
                OrderStatusCodes.RETURN_REJECTED
        ));
        VALID_TRANSITIONS.put(OrderStatusCodes.RETURN_APPROVED, Set.of(
                OrderStatusCodes.RETURNING
        ));
        VALID_TRANSITIONS.put(OrderStatusCodes.RETURNING, Set.of(
                OrderStatusCodes.RETURNED
        ));
        VALID_TRANSITIONS.put(OrderStatusCodes.RETURNED, Set.of(
                OrderStatusCodes.REFUNDED
        ));
        VALID_TRANSITIONS.put(OrderStatusCodes.RETURN_REJECTED, Set.of());
        VALID_TRANSITIONS.put(OrderStatusCodes.REFUNDED, Set.of());
        VALID_TRANSITIONS.put(OrderStatusCodes.CANCELLED, Set.of(
                OrderStatusCodes.REFUNDED
        ));
    }

    /**
     * Kiểm tra xem có thể chuyển từ fromStatus sang toStatus hay không
     * @param fromStatus - Trạng thái hiện tại
     * @param toStatus - Trạng thái muốn chuyển đến
     * @return true nếu có thể chuyển đổi, false nếu không
     */
    public boolean isValidStatusTransition(String fromStatus, String toStatus) {
        // Kiểm tra null hoặc empty
        if (fromStatus == null || toStatus == null ||
                fromStatus.trim().isEmpty() || toStatus.trim().isEmpty()) {
            return false;
        }

        // Kiểm tra status có tồn tại không
        if (!STATUS_ORDER.containsKey(fromStatus) || !STATUS_ORDER.containsKey(toStatus)) {
            return false;
        }

        // Nếu status giống nhau thì không cần chuyển đổi
        if (fromStatus.equals(toStatus)) {
            return false;
        }

        // CANCELLED là trạng thái đặc biệt - có thể cancel từ hầu hết các trạng thái
        if (OrderStatusCodes.CANCELLED.equals(toStatus)) {
            // Không thể cancel từ các trạng thái đã hoàn thành
            return !Set.of(
                    OrderStatusCodes.COMPLETED,
                    OrderStatusCodes.REFUNDED,
                    OrderStatusCodes.RETURNED,
                    OrderStatusCodes.RETURN_REJECTED
            ).contains(fromStatus);
        }

        // Không thể chuyển từ CANCELLED sang trạng thái khác (trừ REFUNDED)
        if (OrderStatusCodes.CANCELLED.equals(fromStatus) && !OrderStatusCodes.REFUNDED.equals(toStatus)) {
            return false;
        }

        // Kiểm tra logic thanh toán: chỉ có thể đến các trạng thái sau khi thanh toán thành công
        if (this.requiresPayment(toStatus) && !this.isPaymentSuccessful(fromStatus)) {
            return false;
        }

        // Kiểm tra xem có thể chuyển đổi hợp lệ không
        Set<String> validNextStatuses = VALID_TRANSITIONS.get(fromStatus);
        return validNextStatuses != null && validNextStatuses.contains(toStatus);
    }

    /**
     * Kiểm tra xem toStatus có đứng sau fromStatus trong thứ tự không
     * @param fromStatus - Trạng thái đầu
     * @param toStatus - Trạng thái cuối
     * @return true nếu toStatus đứng sau fromStatus
     */
    public boolean isStatusInOrder(String fromStatus, String toStatus) {
        // Kiểm tra null hoặc empty
        if (fromStatus == null || toStatus == null ||
                fromStatus.trim().isEmpty() || toStatus.trim().isEmpty()) {
            return false;
        }

        // Kiểm tra status có tồn tại không
        if (!STATUS_ORDER.containsKey(fromStatus) || !STATUS_ORDER.containsKey(toStatus)) {
            return false;
        }

        Integer fromOrder = STATUS_ORDER.get(fromStatus);
        Integer toOrder = STATUS_ORDER.get(toStatus);

        return toOrder > fromOrder;
    }

    /**
     * Lấy danh sách các trạng thái có thể chuyển đổi từ status hiện tại
     * @param currentStatus - Trạng thái hiện tại
     * @return Set các trạng thái có thể chuyển đến
     */
    public Set<String> getValidNextStatuses(String currentStatus) {
        if (currentStatus == null || currentStatus.trim().isEmpty()) {
            return new HashSet<>();
        }

        Set<String> validStatuses = VALID_TRANSITIONS.get(currentStatus);
        return validStatuses != null ? new HashSet<>(validStatuses) : new HashSet<>();
    }

    /**
     * Kiểm tra xem status có phải là trạng thái cuối không
     * @param status - Trạng thái cần kiểm tra
     * @return true nếu là trạng thái cuối
     */
    public boolean isFinalStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }

        return Set.of(
                OrderStatusCodes.COMPLETED,
                OrderStatusCodes.REFUNDED,
                OrderStatusCodes.RETURN_REJECTED
        ).contains(status);
    }

    /**
     * Kiểm tra xem status có cần thanh toán trước không
     * @param status - Trạng thái cần kiểm tra
     * @return true nếu cần thanh toán trước
     */
    public boolean requiresPayment(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }

        // Các trạng thái cần thanh toán trước
        return Set.of(
                OrderStatusCodes.PREPARING,
                OrderStatusCodes.DELIVERING,
                OrderStatusCodes.DELIVERED,
                OrderStatusCodes.COMPLETED,
                OrderStatusCodes.RETURN_REQUESTED,
                OrderStatusCodes.RETURN_APPROVED,
                OrderStatusCodes.RETURNING,
                OrderStatusCodes.RETURNED,
                OrderStatusCodes.REFUNDED
        ).contains(status);
    }

    /**
     * Kiểm tra xem đã thanh toán thành công chưa
     * @param status - Trạng thái hiện tại
     * @return true nếu đã thanh toán thành công
     */
    public boolean isPaymentSuccessful(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }

        // Các trạng thái cho thấy đã thanh toán thành công
        return Set.of(
                OrderStatusCodes.PAYMENT_CONFIRMATION,
                OrderStatusCodes.PREPARING,
                OrderStatusCodes.DELIVERING,
                OrderStatusCodes.DELIVERED,
                OrderStatusCodes.COMPLETED,
                OrderStatusCodes.RETURN_REQUESTED,
                OrderStatusCodes.RETURN_APPROVED,
                OrderStatusCodes.RETURNING,
                OrderStatusCodes.RETURNED,
                OrderStatusCodes.REFUNDED
        ).contains(status);
    }
    /**
     * Trả về trạng thái thanh toán của đơn hàng
     * @param status - Trạng thái hiện tại của đơn hàng
     * @return Trạng thái thanh toán ("PAID", "PAYMENT_FAILED", "PAYMENT_CANCELLED", "UNPAID") hoặc null nếu không xác định
     * @throws IllegalArgumentException nếu trạng thái không hợp lệ
     */
    public String isPaymentCode(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Order status cannot be null or empty");
        }

        if (!STATUS_ORDER.containsKey(status)) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }

        // Nếu đang ở trạng thái PENDING → chưa thanh toán
        if (OrderStatusCodes.PENDING.equals(status)) {
            return OrderStatusCodes.UNPAID;
        }

        // Các trạng thái thanh toán trực tiếp
        if (Set.of(
                OrderStatusCodes.PAYMENT_FAILED,
                OrderStatusCodes.PAYMENT_CANCELLED,
                OrderStatusCodes.PAYMENT_CONFIRMATION
        ).contains(status)) {
            return status;
        }

        // Các trạng thái sau PAID ngụ ý đã thanh toán thành công
        if (isPaymentSuccessful(status)) {
            return OrderStatusCodes.PAYMENT_CONFIRMATION;
        }

        // Các trạng thái khác không liên quan thanh toán
        return null;
    }


    /**
     * Kiểm tra xem có thể hoàn tiền không
     * @param status - Trạng thái hiện tại
     * @return true nếu có thể hoàn tiền
     */
    public boolean canRefund(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }

        // Chỉ có thể hoàn tiền khi đã thanh toán và bị cancel hoặc đã trả hàng
        return Set.of(OrderStatusCodes.CANCELLED, OrderStatusCodes.RETURNED).contains(status);
    }

    /**
     * Kiểm tra xem có cần hoàn tiền không (đã thanh toán và bị cancel)
     * @param currentStatus - Trạng thái hiện tại
     * @param previousStatus - Trạng thái trước đó (để biết đã thanh toán chưa)
     * @return true nếu cần hoàn tiền
     */
    public boolean needsRefund(String currentStatus, String previousStatus) {
        if (currentStatus == null || previousStatus == null) {
            return false;
        }

        // Nếu đã cancel và trước đó đã thanh toán thành công
        return OrderStatusCodes.CANCELLED.equals(currentStatus) && isPaymentSuccessful(previousStatus);
    }

    /**
     * Kiểm tra xem đơn hàng có đang chờ xác nhận từ người bán không
     * @param status - Trạng thái hiện tại
     * @return true nếu đang chờ xác nhận
     */
    public boolean isPendingSellerConfirmation(String status) {
        return OrderStatusCodes.PAYMENT_CONFIRMATION.equals(status);
    }

    /**
     * Kiểm tra xem người bán có thể xác nhận đơn hàng không
     * @param status - Trạng thái hiện tại
     * @return true nếu có thể xác nhận
     */
    public boolean canSellerConfirm(String status) {
        return OrderStatusCodes.PAYMENT_CONFIRMATION.equals(status);
    }

    /**
     * Kiểm tra xem có thể hủy đơn hàng không (với hoàn tiền nếu đã thanh toán)
     * @param status - Trạng thái hiện tại
     * @return true nếu có thể hủy
     */
    public boolean canCancel(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }

        // Không thể hủy từ các trạng thái đã hoàn thành
        return !Set.of(
                OrderStatusCodes.COMPLETED,
                OrderStatusCodes.REFUNDED,
                OrderStatusCodes.RETURNED,
                OrderStatusCodes.RETURN_REJECTED,
                OrderStatusCodes.CANCELLED
        ).contains(status);
    }
}