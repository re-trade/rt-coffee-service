package org.retrade.main.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.retrade.main.model.constant.ProductConditionEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@Getter
@Setter
public class ProductRetradeBaseResponse {
    private String id;
    private String name;
    private String sellerId;
    private String sellerShopName;
    private String shortDescription;
    private String description;
    private String thumbnail;
    private Set<String> productImages;
    private String brandId;
    private String brand;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate warrantyExpiryDate;
    private String model;
    private BigDecimal currentPrice;
    private List<CategoryBaseResponse> categories;
    private ProductConditionEnum condition;
    private Set<String> tags;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private Long retradeQuantity;
    private Set<OrderItemRetradeResponse> orderItemRetrades;
}
