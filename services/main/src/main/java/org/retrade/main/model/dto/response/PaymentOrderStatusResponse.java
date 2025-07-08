package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderStatusResponse {
    private Boolean paid;
    private Set<String> relatedComboIds;
    private String orderId;
}
