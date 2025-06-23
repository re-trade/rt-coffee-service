package org.retrade.main.model.dto.response;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.constant.PaymentStatusEnum;
import org.retrade.main.model.entity.OrderEntity;
import org.retrade.main.model.entity.PaymentMethodEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponse {
    private OrderEntity order;
    private PaymentMethodEntity paymentMethod;
    private BigDecimal paymentTotal;
    private String paymentContent;
    private String paymentCode;
    private PaymentStatusEnum paymentStatus;
    private LocalDateTime paymentTime;
}
