package org.retrade.main.model.dto.request;


import lombok.Data;

@Data
public class CustomerContactRequest {
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
