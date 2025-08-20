package org.retrade.prover.service;

import org.retrade.prover.model.dto.IdentityVerificationResult;
import org.retrade.prover.model.dto.OCRGrpcResponse;

import java.io.File;

public interface OCRService {
    OCRGrpcResponse scanCCCD(File frontImageFile, File backImageFile);
}
