package org.retrade.main.model.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String resolutionStatus;
    private String resolutionDetail;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolutionDate;

}
