package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
