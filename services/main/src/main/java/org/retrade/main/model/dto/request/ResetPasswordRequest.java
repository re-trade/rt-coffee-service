package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.annotation.PasswordMatches;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatches(message = "Passwords do not match")
public class ResetPasswordRequest {
    @NotBlank(message = "Old password is required")
    private String oldPassword;
    @NotBlank(message = "New password is required")
    private String newPassword;
    @NotBlank(message = "Confirm new password is required")
    private String confirmNewPassword;
}
