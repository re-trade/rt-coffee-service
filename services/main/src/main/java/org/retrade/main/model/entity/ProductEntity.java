package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "products")
public class ProductEntity extends BaseSQLEntity {
    private String name;
    @ManyToOne(targetEntity = SellerEntity.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private SellerEntity seller;
    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "thumbnail", length = 256)
    private String thumbnail;
    private Set<String> productImages;
    @Column(name = "brand", length = 128, nullable = false)
    private String brand;
    @Column(name = "discount", nullable = false)
    private String discount;
    @Column(name = "model", nullable = false, length = 128)
    private String model;
    @Column(name = "current_price", nullable = false)
    private BigDecimal currentPrice;
    private Set<String> categories;
    private Set<String> keywords;
    private Set<String> tags;
    @Column(name = "verified", nullable = false)
    private Boolean verified;
}
