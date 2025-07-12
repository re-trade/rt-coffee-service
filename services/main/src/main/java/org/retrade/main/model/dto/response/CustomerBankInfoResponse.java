package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBankInfoResponse {
    private String id;
    private String bankCode;
    private String bankName;
    private String accountNumber;
    private String userBankName;
}
