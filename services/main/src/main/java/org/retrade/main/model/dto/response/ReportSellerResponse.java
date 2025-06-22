package org.retrade.main.model.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.entity.AccountEntity;
import org.retrade.main.model.entity.OrderComboEntity;
import org.retrade.main.model.entity.SellerEntity;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportSellerResponse {

    private String reportSellerId;

    private String customerId;

    private String productId;
    private String orderId;
    private String sellerId;

    private String typeReport;
    private String content;
    private String image;
    private LocalDateTime createdAt;

    private String resolutionStatus;
    private String resolutionDetail;
    private LocalDateTime resolutionDate;
    private String adminId;

}
