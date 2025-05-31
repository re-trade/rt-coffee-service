package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodResponse {
    private String id;
    private String name;
    private String code;
    private String type;
    private String description;
    private String imgUrl;
}
