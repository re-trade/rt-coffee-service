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
public class CustomerAccountRegisterRequest {

    @NotEmpty(message = "Username must not be empty")
    @NotNull(message = "Username is required")
    private String username;

    @NotEmpty(message = "Password must not be empty")
    @NotNull(message = "Password is required")
    private String password;

    @NotEmpty(message = "Re-entered password must not be empty")
    @NotNull(message = "Re-entered password is required")
    private String rePassword;

    @NotEmpty(message = "Email must not be empty")
    @NotNull(message = "Email is required")
    private String email;

    @NotEmpty(message = "First name must not be empty")
    @NotNull(message = "First name is required")
    private String firstName;

    @NotEmpty(message = "Last name must not be empty")
    @NotNull(message = "Last name is required")
    private String lastName;

    @NotEmpty(message = "Phone number must not be empty")
    @NotNull(message = "Phone number is required")
    private String phone;

    @NotNull(message = "Gender is required")
    private Integer gender;

    @NotEmpty(message = "Address must not be empty")
    @NotNull(message = "Address is required")
    private String address;
}
