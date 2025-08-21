package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    private String avatarUrl;
    @Min(value = 0, message = "Gender must be between 0 and 2")
    @NotNull(message = "Gender cannot be null")
    @Max(value = 2, message = "Gender must be between 0 and 2")
    private Integer gender;
}
