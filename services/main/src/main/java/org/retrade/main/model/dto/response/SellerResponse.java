package org.retrade.main.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.constant.IdentityVerifiedStatusEnum;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SellerResponse {
    private String id;
    private String accountId;
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
    private Double avgVote;
    private IdentityVerifiedStatusEnum identityVerifiedStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
