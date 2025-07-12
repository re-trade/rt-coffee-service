package org.retrade.main.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class OrderStatusValidator {
    private static final Map<String, Integer> STATUS_ORDER = new HashMap<>();

    static {
        STATUS_ORDER.put("PENDING", 1);
        STATUS_ORDER.put("CONFIRMED", 2);
        STATUS_ORDER.put("PAYMENT_FAILED", 3);
        STATUS_ORDER.put("PAYMENT_CANCELLED", 4);
        STATUS_ORDER.put("PAYMENT_CONFIRMATION", 5);
        STATUS_ORDER.put("PREPARING", 6);
        STATUS_ORDER.put("DELIVERING", 7);
        STATUS_ORDER.put("DELIVERED", 8);
        STATUS_ORDER.put("COMPLETED", 9);
        STATUS_ORDER.put("RETURN_REQUESTED", 10);
        STATUS_ORDER.put("RETURN_APPROVED", 11);
        STATUS_ORDER.put("RETURNING", 12);
        STATUS_ORDER.put("RETURNED", 13);
        STATUS_ORDER.put("RETURN_REJECTED", 14);
        STATUS_ORDER.put("REFUNDED", 15);
        STATUS_ORDER.put("CANCELLED", 16);
    }

    // Định nghĩa các trạng thái có thể chuyển đổi hợp lệ
    private static final Map<String, Set<String>> VALID_TRANSITIONS = new HashMap<>();

    static {
        VALID_TRANSITIONS.put("PENDING", Set.of("CONFIRMED", "CANCELLED"));
        VALID_TRANSITIONS.put("CONFIRMED", Set.of("PAYMENT_FAILED", "PAYMENT_CANCELLED", "PAYMENT_CONFIRMATION", "CANCELLED"));
        VALID_TRANSITIONS.put("PAYMENT_FAILED", Set.of("PAYMENT_CONFIRMATION", "CANCELLED"));
        VALID_TRANSITIONS.put("PAYMENT_CANCELLED", Set.of("CANCELLED"));
        VALID_TRANSITIONS.put("PAYMENT_CONFIRMATION", Set.of("PREPARING", "CANCELLED"));
        VALID_TRANSITIONS.put("PREPARING", Set.of("DELIVERING", "CANCELLED"));
        VALID_TRANSITIONS.put("DELIVERING", Set.of("DELIVERED", "CANCELLED"));
        VALID_TRANSITIONS.put("DELIVERED", Set.of("COMPLETED", "RETURN_REQUESTED", "CANCELLED"));
        VALID_TRANSITIONS.put("COMPLETED", Set.of("RETURN_REQUESTED", "CANCELLED"));
        VALID_TRANSITIONS.put("RETURN_REQUESTED", Set.of("RETURN_APPROVED", "RETURN_REJECTED", "CANCELLED"));
        VALID_TRANSITIONS.put("RETURN_APPROVED", Set.of("RETURNING", "CANCELLED"));
        VALID_TRANSITIONS.put("RETURNING", Set.of("RETURNED", "CANCELLED"));
        VALID_TRANSITIONS.put("RETURNED", Set.of("REFUNDED", "CANCELLED"));
        VALID_TRANSITIONS.put("RETURN_REJECTED", Set.of("CANCELLED")); // Có thể cancel
        VALID_TRANSITIONS.put("REFUNDED", Set.of()); // Trạng thái cuối
        VALID_TRANSITIONS.put("CANCELLED", Set.of("REFUNDED")); // Có thể hoàn tiền sau khi cancel
    }

    /**
     * Kiểm tra xem có thể chuyển từ fromStatus sang toStatus hay không
     * @param fromStatus - Trạng thái hiện tại
     * @param toStatus - Trạng thái muốn chuyển đến
     * @return true nếu có thể chuyển đổi, false nếu không
     */
    public static boolean isValidStatusTransition(String fromStatus, String toStatus) {
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

        // CANCELLED là trạng thái đặc biệt - có thể cancel bất cứ lúc nào
        if ("CANCELLED".equals(toStatus)) {
            return true;
        }

        // Không thể chuyển từ CANCELLED sang trạng thái khác (trừ REFUNDED)
        if ("CANCELLED".equals(fromStatus) && !"REFUNDED".equals(toStatus)) {
            return false;
        }

        // Kiểm tra logic thanh toán: chỉ có thể đến các trạng thái sau khi thanh toán thành công
        if (requiresPayment(toStatus) && !isPaymentSuccessful(fromStatus)) {
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
    public static boolean isStatusInOrder(String fromStatus, String toStatus) {
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
    public static Set<String> getValidNextStatuses(String currentStatus) {
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
    public static boolean isFinalStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }

        return Set.of("COMPLETED", "REFUNDED", "CANCELLED", "RETURN_REJECTED").contains(status);
    }

    /**
     * Kiểm tra xem status có cần thanh toán trước không
     * @param status - Trạng thái cần kiểm tra
     * @return true nếu cần thanh toán trước
     */
    public static boolean requiresPayment(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }

        // Các trạng thái cần thanh toán trước
        return Set.of("PREPARING", "DELIVERING", "DELIVERED", "COMPLETED",
                        "RETURN_REQUESTED", "RETURN_APPROVED", "RETURNING", "RETURNED", "REFUNDED")
                .contains(status);
    }

    /**
     * Kiểm tra xem đã thanh toán thành công chưa
     * @param status - Trạng thái hiện tại
     * @return true nếu đã thanh toán thành công
     */
    public static boolean isPaymentSuccessful(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }

        // Các trạng thái cho thấy đã thanh toán thành công
        return Set.of("PAYMENT_CONFIRMATION", "PREPARING", "DELIVERING", "DELIVERED",
                        "COMPLETED", "RETURN_REQUESTED", "RETURN_APPROVED", "RETURNING",
                        "RETURNED", "REFUNDED")
                .contains(status);
    }

    /**
     * Kiểm tra xem có thể hoàn tiền không
     * @param status - Trạng thái hiện tại
     * @return true nếu có thể hoàn tiền
     */
    public static boolean canRefund(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }

        // Chỉ có thể hoàn tiền khi đã thanh toán và bị cancel hoặc đã trả hàng
        return Set.of("CANCELLED", "RETURNED").contains(status);
    }

    /**
     * Kiểm tra xem có cần hoàn tiền không (đã thanh toán và bị cancel)
     * @param currentStatus - Trạng thái hiện tại
     * @param previousStatus - Trạng thái trước đó (để biết đã thanh toán chưa)
     * @return true nếu cần hoàn tiền
     */
    public static boolean needsRefund(String currentStatus, String previousStatus) {
        if (currentStatus == null || previousStatus == null) {
            return false;
        }

        // Nếu đã cancel và trước đó đã thanh toán thành công
        return "CANCELLED".equals(currentStatus) && isPaymentSuccessful(previousStatus);
    }
}
