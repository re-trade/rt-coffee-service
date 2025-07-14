package org.retrade.main.service;

import org.retrade.main.model.dto.request.VietQrGenerateRequest;

public interface VietQRService {
    String generateQr(VietQrGenerateRequest request);
}
