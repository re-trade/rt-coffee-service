package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerContactResponse {
    private String id;
    private String customerName;
    private String phone;
    private String state;
    private String country;
    private String district;
    private String ward;
    private String addressLine;
    private String name;
    private Boolean defaulted;
    private Integer type;
}
