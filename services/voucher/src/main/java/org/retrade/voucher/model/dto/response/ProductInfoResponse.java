package org.retrade.voucher.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfoResponse {
    private String id;
    private String name;
    private String sellerId;
    private String sellerShopName;
    private String shortDescription;
    private String description;
    private String thumbnail;
    private List<String> productImages;
    private String brand;
    private String discount;
    private String model;
    private BigDecimal currentPrice;
    private List<String> categories;
    private List<String> keywords;
    private List<String> tags;
    private Boolean verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
