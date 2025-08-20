package org.retrade.prover.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.proto.identity.IdentityCardResponse;
import org.retrade.prover.client.IdentityServiceClient;
import org.retrade.prover.model.dto.OCRGrpcResponse;
import org.retrade.prover.model.other.IdentityCardOCRWrapper;
import org.retrade.prover.service.OCRService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class OCRServiceImpl implements OCRService {
    private final IdentityServiceClient identityServiceClient;

    @Override
    public OCRGrpcResponse scanCCCD(File frontImageFile, File backImageFile) {
        var identityVerificationResult = new OCRGrpcResponse();
        try {
            var base64FrontImage = convertImageToBase64(frontImageFile);
            var base64BackImage = convertImageToBase64(backImageFile);
            var backImageResponse = identityServiceClient.getCardByBase64Image(base64BackImage);
            wrapperIdentityForFace(backImageResponse, identityVerificationResult);
            var frontImageResponse = identityServiceClient.getCardByBase64Image(base64FrontImage);
            wrapperIdentityForFace(frontImageResponse, identityVerificationResult);
            identityVerificationResult.setVerificationSuccessful(identityVerificationResult.getFrontSide() != null);
            return identityVerificationResult;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return OCRGrpcResponse.builder()
                    .verificationSuccessful(false)
                    .build();
        }
    }

    private String convertImageToBase64(File image) throws IOException {
        var imageContent = Files.readAllBytes(image.toPath());
        var imageEncode =  Base64.getEncoder().encodeToString(imageContent);
        String mimeType = Files.probeContentType(image.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        return "data:" + mimeType + ";base64," + imageEncode;
    }

    private void wrapperIdentityForFace (IdentityCardResponse cardResponse, OCRGrpcResponse ocrResponse) {
        if (cardResponse.getSuccess()){
            var card = cardResponse.getCard();
            if ("N/A".equals(card.getPlaceOfOrigin()) || "N/A".equals(card.getPlaceOfResidence())) {
                ocrResponse.setBackSide(IdentityCardOCRWrapper.builder()
                                .identityNumber(card.getIdentityNumber())
                                .fullName(card.getFullName())
                                .dateOfBirth(card.getDateOfBirth())
                        .build());
            } else {
                ocrResponse.setFrontSide(
                        IdentityCardOCRWrapper.builder()
                                .identityNumber(card.getIdentityNumber())
                                .fullName(card.getFullName())
                                .dateOfBirth(card.getDateOfBirth())
                                .sex(card.getSex())
                                .nationality(card.getNationality())
                                .placeOfResidence(card.getPlaceOfResidence())
                                .placeOfOrigin(card.getPlaceOfOrigin())
                                .dateOfExpiry(card.getDateOfExpiry())
                                .build()
                );
            }
        }
    }
}
