package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAddressRequest {
    
    @NotNull(message = "Customer name is required")
    @NotEmpty(message = "Customer name cannot be empty")
    @Size(max = 255, message = "Customer name must not exceed 255 characters")
    private String customerName;
    
    @NotNull(message = "Phone number is required")
    @NotEmpty(message = "Phone number cannot be empty")
    @Pattern(regexp = "^[0-9+\\-\\s()]{10,15}$", message = "Invalid phone number format")
    @Size(max = 12, message = "Phone number must not exceed 12 characters")
    private String phone;
    
    @Size(max = 20, message = "State must not exceed 20 characters")
    private String state;
    
    @Size(max = 20, message = "Country must not exceed 20 characters")
    private String country;
    
    @Size(max = 20, message = "District must not exceed 20 characters")
    private String district;
    
    @Size(max = 20, message = "Ward must not exceed 20 characters")
    private String ward;
    
    @NotNull(message = "Address line is required")
    @NotEmpty(message = "Address line cannot be empty")
    @Size(max = 500, message = "Address line must not exceed 500 characters")
    private String addressLine;
    
    @Size(max = 10, message = "Postal code must not exceed 10 characters")
    private String postalCode;
}
