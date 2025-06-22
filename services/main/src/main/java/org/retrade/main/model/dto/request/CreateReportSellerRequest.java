package org.retrade.main.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReportSellerRequest {
    private String sellerId;

    private String typeReport;

    private String content;

    private String orderId;

    private String productId;

    private String image;
}
