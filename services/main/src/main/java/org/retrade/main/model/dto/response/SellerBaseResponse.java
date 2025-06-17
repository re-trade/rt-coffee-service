package org.retrade.main.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String addressLine;
    private String district;
    private String ward;
    private String state;
    private String avatarUrl;
    private String email;
    private String background;
    private String phoneNumber;
    private Boolean verified;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
