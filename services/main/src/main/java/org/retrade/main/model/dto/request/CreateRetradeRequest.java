package org.retrade.main.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRetradeRequest {
    private String orderItemId;
    private Integer quantity;
    private BigDecimal price;
    private String shortDescription;
    private String description;
    private String thumbnail;
    private String status;
}
