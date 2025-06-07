package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerContactRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "Ward is required")
    private String ward;

    @NotBlank(message = "Address line is required")
    private String addressLine;

    @NotBlank(message = "Contact name is required")
    private String name;

    @NotNull(message = "Defaulted status must be specified")
    private Boolean defaulted;

    @NotNull(message = "Contact type is required")
    private Integer type;
}
