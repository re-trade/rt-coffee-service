package org.retrade.main.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerRegisterRequest {
    private String shopName;
    private String description;
    private Integer businessType;
    private String address;
    private String avatarUrl;
    private String taxCode;
    private String email;
    private String background;
    private String phoneNumber;
    private String identityNumber;
}
