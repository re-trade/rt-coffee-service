package org.retrade.main.model.dto.request;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ForgotPasswordRequest {
    private String token;
    private String password;
    private String rePassword;
}
