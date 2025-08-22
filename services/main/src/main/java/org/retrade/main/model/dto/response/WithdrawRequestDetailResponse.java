package org.retrade.main.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.constant.WithdrawStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequestDetailResponse {
    private String id;
    private BigDecimal amount;
    private WithdrawStatusEnum status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedDate;
    private String bankBin;
    private String bankName;
    private String bankUrl;
    private String proveUrl;
    private String cancelReason;
    private String username;
    private String customerName;
    private String customerAvatarUrl;
    private String customerPhone;
    private String customerEmail;
    private String sellerName;
    private String sellerAvatarUrl;
    private String sellerPhone;
    private String sellerEmail;
}
