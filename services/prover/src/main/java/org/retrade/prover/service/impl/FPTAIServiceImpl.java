package org.retrade.prover.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.retrade.prover.config.FPTApiConfig;
import org.retrade.prover.model.dto.FptAiApiResponse;
import org.retrade.prover.model.dto.FptAiData;
import org.retrade.prover.model.dto.IdCardInfo;
import org.retrade.prover.model.dto.IdentityVerificationResult;
import org.retrade.prover.service.FPTAIService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FPTAIServiceImpl implements FPTAIService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final FPTApiConfig config;
    @Override
    public IdentityVerificationResult scanCCCD(File frontImageFile, File backImageFile) {
        IdentityVerificationResult result = new IdentityVerificationResult();
        result.setVerificationSuccessful(true);
        IdCardInfo frontInfo = processImageForIdRecognition(frontImageFile, "FRONT");
        if (frontInfo != null) {
            result.setFrontSide(frontInfo);
        } else {
            result.setVerificationSuccessful(false);
            result.setErrorMessage("Failed to process front image.");
            return result;
        }
        IdCardInfo backInfo = processImageForIdRecognition(backImageFile, "BACK");
        if (backInfo != null) {
            result.setBackSide(backInfo);
        } else {
            result.setVerificationSuccessful(false);
            result.setErrorMessage("Failed to process back image.");
            return result;
        }
        return result;
    }


    private IdCardInfo processImageForIdRecognition(File imageFile, String cardSide) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("api_key", config.getApiKey());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new FileSystemResource(imageFile));
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    config.getBaseUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String rawJson = response.getBody();
                FptAiApiResponse apiResponse = objectMapper.readValue(rawJson, FptAiApiResponse.class);

                if (apiResponse.getErrorCode() == 0) {
                    return mapFptAiDataToIdCardInfo(apiResponse.getData(), rawJson);
                } else {
                    System.err.println("FPT.AI API Error for " + cardSide + ": " + apiResponse.getErrorMessage());
                    return null;
                }
            } else {
                System.err.println("FPT.AI API call failed for " + cardSide + ": " + response.getStatusCode() + " - " + response.getBody());
                return null;
            }
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Client Error for " + cardSide + ": " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return null;
        } catch (IOException e) {
            System.err.println("JSON parsing error for " + cardSide + ": " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during FPT.AI API call for " + cardSide + ": " + e.getMessage());
            return null;
        }
    }

    private IdCardInfo mapFptAiDataToIdCardInfo(List<FptAiData> fptAiData, String rawApiResponse) {
        if (fptAiData == null || fptAiData.isEmpty()) {
            return null;
        }
        IdCardInfo idCardInfo = new IdCardInfo();
        var detail = fptAiData.getFirst();
        idCardInfo.setRawApiResponse(rawApiResponse);
        idCardInfo.setDateOfIssue(detail.getIssueDate());
        idCardInfo.setPlaceOfIssue("N/A");
        idCardInfo.setDocumentType(detail.getType());
        idCardInfo.setDateOfExpiry(detail.getDoe());
        if (detail.getMrzDetail() != null && "chip_back".equals(detail.getType())) {
            var mrzDetail = detail.getMrzDetail();
            idCardInfo.setSide("BACK");
            idCardInfo.setIdNumber(mrzDetail.getId());
            idCardInfo.setFullName(mrzDetail.getName());
            idCardInfo.setDateOfBirth(mrzDetail.getDob());
            idCardInfo.setGender(mrzDetail.getSex());
            idCardInfo.setPlaceOfOrigin("N/A");
            idCardInfo.setPlaceOfResidence(mrzDetail.getNationality());
        } else {
            idCardInfo.setSide("FRONT");
            idCardInfo.setIdNumber(detail.getId());
            idCardInfo.setFullName(detail.getName());
            idCardInfo.setDateOfBirth(detail.getDob());
            idCardInfo.setGender(detail.getSex());
            idCardInfo.setPlaceOfOrigin(detail.getHome());
            idCardInfo.setPlaceOfResidence(detail.getAddress());
        }

        return idCardInfo;
    }
}
