package org.retrade.main.model.other;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentAPICallback {
    private boolean status;
    private Long id;
    private BigDecimal total;
    private String orderInfo;
    private String transactionId;
}
