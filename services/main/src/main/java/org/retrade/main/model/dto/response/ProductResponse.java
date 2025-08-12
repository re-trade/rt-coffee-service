package org.retrade.main.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.constant.ProductConditionEnum;
import org.retrade.main.model.constant.ProductStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
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
    private Integer quantity;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate warrantyExpiryDate;
    private String model;
    private BigDecimal currentPrice;
    private List<CategoryBaseResponse> categories;
    private ProductConditionEnum condition;
    private ProductStatusEnum status;
    private Set<String> tags;
    private Boolean verified;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private Double avgVote;
    private boolean retraded;
}
