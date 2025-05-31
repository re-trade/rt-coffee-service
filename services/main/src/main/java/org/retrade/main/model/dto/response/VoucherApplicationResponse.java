package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherApplicationResponse {
    private String voucherId;
    private String voucherCode;
    private String voucherType;
    private BigDecimal discountAmount;
    private String discountType;
    private Boolean applied;
    private String message;
}
