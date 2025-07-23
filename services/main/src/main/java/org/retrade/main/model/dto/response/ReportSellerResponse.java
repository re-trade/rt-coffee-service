package org.retrade.main.model.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportSellerResponse {
    private String id;
    private String sellerId;
    private String sellerName;
    private String sellerAvatarUrl;

    private String customerId;
    private String productId;
    private String orderId;


    private String typeReport;
    private String content;
    private LocalDateTime createdAt;

    private String resolutionStatus;
    private String resolutionDetail;
    private LocalDateTime resolutionDate;

}
