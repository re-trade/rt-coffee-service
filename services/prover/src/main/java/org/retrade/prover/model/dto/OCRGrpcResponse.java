package org.retrade.prover.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.prover.model.other.IdentityCardOCRWrapper;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OCRGrpcResponse {
    private IdentityCardOCRWrapper frontSide;
    private IdentityCardOCRWrapper backSide;
    private boolean verificationSuccessful;
    private String errorMessage;
}
