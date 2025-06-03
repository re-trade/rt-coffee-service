package org.retrade.voucher.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherClaimSimpleResponse {
    private String id;
    private String code;
    private String type;
    private Double discount;
    private LocalDateTime expiryDate;
    private BigDecimal minSpend;
    private String status;
    private String title;
    private String description;
}
