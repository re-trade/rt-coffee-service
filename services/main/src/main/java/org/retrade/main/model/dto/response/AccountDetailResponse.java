package org.retrade.main.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountDetailResponse {
    private String id;
    private String username;
    private String email;
    private boolean enabled;
    private boolean locked;
    private boolean using2FA;
    private boolean changedUsername;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLogin;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinInDate;
    private List<String> roles;
    private CustomerBaseResponse customerProfile;
    private SellerBaseResponse sellerProfile;
}
