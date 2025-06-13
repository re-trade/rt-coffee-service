package org.retrade.main.model.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CCCDVerificationResultMessage {
    private String sellerId;
    private Boolean accepted;
    private String message;
}
