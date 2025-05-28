package org.retrade.main.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "roles")
public class RoleEntity extends BaseSQLEntity {
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "code", nullable = false, unique = true)
    private String code;
}
