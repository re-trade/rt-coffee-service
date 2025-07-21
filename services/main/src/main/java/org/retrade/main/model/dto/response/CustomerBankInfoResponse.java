package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBankInfoResponse {
    private String id;
    private String bankBin;
    private String bankName;
    private String accountNumber;
    private String userBankName;
    private Timestamp addedDate;
}
