package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.constant.WithdrawStatusEnum;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequestBaseResponse {
    private String id;
    private BigDecimal amount;
    private WithdrawStatusEnum status;
    private Timestamp processedDate;
    private String bankBin;
    private String bankName;
    private String bankUrl;
}
