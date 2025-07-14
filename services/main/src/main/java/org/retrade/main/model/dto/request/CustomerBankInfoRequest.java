package org.retrade.main.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBankInfoRequest {
    private String accountNumber;
    private String bankBin;
    private String bankName;
    private String userBankName;
}
