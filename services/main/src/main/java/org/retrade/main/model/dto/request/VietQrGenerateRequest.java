package org.retrade.main.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VietQrGenerateRequest {
    private String accountNo;
    private String accountName;
    private String acqId;
    private String addInfo;
    private Long amount;
    private String template;
}
