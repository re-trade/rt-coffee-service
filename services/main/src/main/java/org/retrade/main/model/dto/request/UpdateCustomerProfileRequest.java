package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerProfileRequest {
    @NotEmpty(message = "First name is required")
    @NotNull(message = "First name cannot be null")
    private String firstName;
    @NotEmpty(message = "Last name is required")
    @NotNull(message = "Last name cannot be null")
    private String lastName;
    @NotEmpty(message = "Phone is required")
    @NotNull(message = "Phone cannot be null")
    private String phone;
    @NotEmpty(message = "Address is required")
    @NotNull(message = "Address cannot be null")
    private String address;
    @NotEmpty(message = "Avatar url is required")
    @NotNull(message = "Avatar url cannot be null")
    private String avatarUrl;
}
