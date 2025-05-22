package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdatePasswordRequest {
    @NotEmpty(message = "Current password is required")
    @NotNull(message = "Current password cannot be null")
    private String currentPassword;
    
    @NotEmpty(message = "New password is required")
    @NotNull(message = "New password cannot be null")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    private String newPassword;
    
    @NotEmpty(message = "Confirm password is required")
    @NotNull(message = "Confirm password cannot be null")
    private String confirmPassword;
}
