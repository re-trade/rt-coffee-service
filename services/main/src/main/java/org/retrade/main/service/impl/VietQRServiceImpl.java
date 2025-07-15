package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.main.config.provider.VietQRConfig;
import org.retrade.main.model.dto.request.VietQrGenerateRequest;
import org.retrade.main.model.dto.response.VietQrGenerateResponse;
import org.retrade.main.repository.redis.VietQrBankRepository;
import org.retrade.main.service.VietQRService;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class VietQRServiceImpl implements VietQRService {
    private final VietQRConfig config;
    private final VietQrBankRepository vietQrBankRepository;

    @Override
    public String generateQr(VietQrGenerateRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", config.getApiKey());
        headers.set("x-client-id", config.getClientId());

        HttpEntity<VietQrGenerateRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<VietQrGenerateResponse> response = restTemplate.exchange(
                config.getUrl(),
                HttpMethod.POST,
                entity,
                VietQrGenerateResponse.class
        );
        VietQrGenerateResponse body = response.getBody();
        if (body != null && "00".equals(body.getCode())) {
            return body.getData().getQrDataURL();
        } else {
            throw new ActionFailedException("Failed to generate QR: " + (body != null ? body.getDesc() : "Unknown error"));
        }
    }
}
