package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SellerBaseResponse {
    private String id;
    private String shopName;
    private String description;
    private Integer businessType;
    private String address;
    private String avatarUrl;
    private String email;
    private String background;
    private String phoneNumber;
    private Boolean verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
