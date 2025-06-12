package org.retrade.prover.service;

import org.retrade.prover.model.dto.IdentityVerificationResult;

import java.io.File;

public interface FPTAIService {
    IdentityVerificationResult scanCCCD(File frontImageFile, File backImageFile);
}
