package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "brands")
public class BrandEntity extends BaseSQLEntity {
    private String name;
    private String description;
    private String imgUrl;
    private Boolean enabled;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "brand_categories",
            joinColumns = @JoinColumn(name = "brand_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<CategoryEntity> categories;
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProductEntity> products;
}
