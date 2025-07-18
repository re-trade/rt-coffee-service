package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "categories")
public class CategoryEntity extends BaseSQLEntity {
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(targetEntity = CategoryEntity.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private CategoryEntity categoryParent;

    @OneToMany(mappedBy = "categoryParent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CategoryEntity> children;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "categories")
    private Set<BrandEntity> brands;

    @Column(name = "visible", nullable = false)
    private Boolean visible;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @ManyToMany(mappedBy = "categories")
    private Set<ProductEntity> products;
}
