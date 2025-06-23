package org.retrade.main.model.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerUpdateRequest {
    private String shopName;
    private String description;
    private String addressLine;
    private String district;
    private String ward;
    private String state;
    private String avatarUrl;
    private String email;
    private String background;
    private String phoneNumber;
}
