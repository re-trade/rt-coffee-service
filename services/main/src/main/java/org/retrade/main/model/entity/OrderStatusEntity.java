package org.retrade.main.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "order_statuses")
public class OrderStatusEntity extends BaseSQLEntity {
    @Column(name = "code", nullable = false, length = 20)
    private String code;
    @Column(name = "name", nullable = false, length = 50)
    private String name;
}
