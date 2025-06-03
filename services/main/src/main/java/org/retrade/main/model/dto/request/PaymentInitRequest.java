package org.retrade.main.model.dto.request;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentInitRequest {
    private String paymentMethodId;
    private String paymentContent;
    private String orderId;
}
