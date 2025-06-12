package org.retrade.prover.service;

import org.retrade.prover.model.dto.IdentityVerificationResult;

public interface FPTAIService {
    IdentityVerificationResult scanCCCD(byte[] frontImage, byte[] backImage);
}
