package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentInitRequest {

    @NotBlank(message = "Payment method ID is required")
    private String paymentMethodId;

    @NotBlank(message = "Payment content is required")
    private String paymentContent;

    @NotBlank(message = "Order ID is required")
    private String orderId;
}
