package org.retrade.voucher.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherClaimResponse {
    private String id;
    private String voucherId;
    private String code;
    private String type;
    private Double discount;
    private LocalDateTime expiryDate;
    private String status;
}
