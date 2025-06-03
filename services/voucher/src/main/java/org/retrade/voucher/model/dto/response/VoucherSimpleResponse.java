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
public class VoucherSimpleResponse {
    private String id;
    private String code;
    private String type;
    private Double discount;
    private LocalDateTime expiryDate;
    private BigDecimal minSpend;
    private String title;
    private String description;
}
