package org.retrade.main.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AccountResponse {
    private String id;
    private String username;
    private String email;
    private boolean enabled;
    private boolean locked;
    private boolean using2FA;
    private LocalDateTime joinInDate;
    private List<String> roles;
}
