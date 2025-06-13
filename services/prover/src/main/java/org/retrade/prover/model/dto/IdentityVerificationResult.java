package org.retrade.prover.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityVerificationResult {
    private IdCardInfo frontSide;
    private IdCardInfo backSide;
    private boolean verificationSuccessful;
    private String errorMessage;
}
