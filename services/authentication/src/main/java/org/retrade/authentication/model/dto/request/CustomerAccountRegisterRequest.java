package org.retrade.authentication.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerAccountRegisterRequest {
    @NotEmpty
    @NotNull
    private String username;
    @NotEmpty
    @NotNull
    private String password;
    @NotEmpty
    @NotNull
    private String rePassword;
    @NotEmpty
    @NotNull
    private String email;
    @NotEmpty
    @NotNull
    private String firstName;
    @NotEmpty
    @NotNull
    private String lastName;
    @NotEmpty
    @NotNull
    private String phone;
    @NotEmpty
    @NotNull
    private String address;
    @NotEmpty
    @NotNull
    private String avatarUrl;
}
