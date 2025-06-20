package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;
import org.retrade.main.model.constant.EProductStatus;

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
    @Column(name = "short_description", nullable = false, columnDefinition = "TEXT")
    private String shortDescription;
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    @Column(name = "thumbnail", length = 256)
    private String thumbnail;
    private Set<String> productImages;
    @Column(name = "brand", length = 128, nullable = false)
    private String brand;
    @Column(name = "discount", nullable = false, columnDefinition = "NUMERIC(5, 2) DEFAULT 0.00 NOT NULL")
    private Double discount;
    @Column(name = "model", nullable = false, length = 128)
    private String model;
    @Column(name = "current_price", nullable = false)
    private BigDecimal currentPrice;
    @ManyToMany(targetEntity = CategoryEntity.class, fetch = FetchType.EAGER)
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "category_id", nullable = false)
    )
    private Set<CategoryEntity> categories;
    private Set<String> keywords;
    private Set<String> tags;
    @Column(name = "verified", nullable = false)
    private Boolean verified;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "enabled", nullable = false, columnDefinition = "SMALLINT DEFAULT 0")
    private EProductStatus status;
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST}, mappedBy = "product")
    private Set<ProductPriceHistoryEntity> productPriceHistories;
}
