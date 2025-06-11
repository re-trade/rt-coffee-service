package org.retrade.main.model.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CCCDVerificationMessage {
    private String messageId;
    private String sellerId;
    private String frontUrl;
    private String backUrl;
    private String identityNumber;
}
