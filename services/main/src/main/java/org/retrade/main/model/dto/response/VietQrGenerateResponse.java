package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VietQrGenerateResponse {
    private String code;
    private String desc;
    private VietQrData data;
    @Data
    public static class VietQrData {
        private String qrDataURL;
        private String qrURL;
    }
}
