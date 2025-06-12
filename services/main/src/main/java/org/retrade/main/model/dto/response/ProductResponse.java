package org.retrade.main.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private String brand;
    private Double discount;
    private String model;
    private BigDecimal currentPrice;
    private Set<String> categories;
    private Set<String> keywords;
    private Set<String> tags;
    private Boolean verified;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
