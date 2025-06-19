package org.retrade.main.model.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePhoneRequest {
    private String newPhone;
    private String passwordConfirm;
}
