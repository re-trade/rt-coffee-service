package org.retrade.main.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RevenueResponse {
    private String orderComboId; // Mã đơn hàng
    private Set<CustomerOrderItemResponse> items;
    private OrderDestinationResponse destination;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate; // Ngày tạo
    private BigDecimal totalPrice; // Tổng tiền
    private Double feePercent; // Phần trăm phí
    private BigDecimal feeAmount; // Tiền phí
    private BigDecimal netAmount; // Thực nhận sau phí
    private OrderStatusResponse status; // Trạng thái
}
