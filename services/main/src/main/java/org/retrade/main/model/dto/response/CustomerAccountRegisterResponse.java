package org.retrade.main.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerAccountRegisterResponse {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String avatarUrl;
}
