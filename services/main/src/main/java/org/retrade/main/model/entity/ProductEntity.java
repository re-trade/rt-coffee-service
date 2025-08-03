package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;
import org.retrade.main.model.constant.ProductConditionEnum;
import org.retrade.main.model.constant.ProductStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Column(name = "img_urls", length = 256)
    private Set<String> productImages;
    @Column(name = "avg_vote", nullable = false, columnDefinition = "NUMERIC(5,2) DEFAULT 0.00")
    private Double avgVote;
    @ManyToOne(targetEntity = BrandEntity.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private BrandEntity brand;
    @Column(name = "quantity", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer quantity;
    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "condition", nullable = false, columnDefinition = "SMALLINT DEFAULT 0")
    private ProductConditionEnum condition;
    @Column(name = "model", nullable = false, length = 128)
    private String model;
    @Column(name = "current_price", nullable = false)
    private BigDecimal currentPrice;
    @ManyToMany(targetEntity = CategoryEntity.class, fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "category_id", nullable = false)
    )
    private Set<CategoryEntity> categories;
    private Set<String> tags;
    @Column(name = "verified", nullable = false)
    private Boolean verified;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", nullable = false, columnDefinition = "SMALLINT DEFAULT 0")
    private ProductStatusEnum status;
    @ManyToOne(targetEntity = ProductEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_parent_id")
    private ProductEntity parentProduct;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentProduct")
    private Set<ProductEntity> childProducts;
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST}, mappedBy = "product")
    private Set<ProductPriceHistoryEntity> productPriceHistories;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    private Set<OrderItemEntity> orderItems;
}
