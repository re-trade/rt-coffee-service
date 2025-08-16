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
    private String orderComboId;
    private Set<CustomerOrderItemResponse> items;
    private OrderDestinationResponse destination;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;
    private BigDecimal totalPrice;
    private Double feePercent;
    private BigDecimal feeAmount;
    private BigDecimal netAmount;
    private OrderStatusResponse status;
}
