package org.retrade.prover.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.prover.model.message.CCCDVerificationMessage;
import org.retrade.prover.model.other.CCCDValidateWrapper;
import org.retrade.prover.service.CCCDProverService;
import org.retrade.prover.service.FPTAIService;
import org.retrade.prover.service.FileEncryptService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CCCDProverServiceImpl implements CCCDProverService {
    private final FPTAIService fptaiService;
    private final FileEncryptService fileEncryptService;

    @Override
    public CCCDValidateWrapper processVerification(CCCDVerificationMessage message) {
        var backFile = fileEncryptService.downloadEncryptedFileWithUrl(message.getBackUrl());
        var frontFile = fileEncryptService.downloadEncryptedFileWithUrl(message.getFrontUrl());
        var fptResult = fptaiService.scanCCCD(frontFile, backFile);
        if (!fptResult.isVerificationSuccessful()) {
            return CCCDValidateWrapper.builder()
                    .valid(false)
                    .message("CCCD verification failed, Please update cccd image and try again")
                    .build();
        }
        var frontSide = fptResult.getFrontSide();
        if (!frontSide.getIdNumber().equals(message.getIdentityNumber())) {
            return CCCDValidateWrapper.builder()
                    .valid(false)
                    .message("CCCD verification failed, Please update cccd id and try again")
                    .build();
        }
        return CCCDValidateWrapper.builder()
                .valid(true)
                .message("Validation Successful")
                .build();
    }
}
