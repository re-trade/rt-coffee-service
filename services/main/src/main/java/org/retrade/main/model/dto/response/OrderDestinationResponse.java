package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDestinationResponse {
    private String customerName;
    private String phone;
    private String state;
    private String country;
    private String district;
    private String ward;
    private String addressLine;
}
