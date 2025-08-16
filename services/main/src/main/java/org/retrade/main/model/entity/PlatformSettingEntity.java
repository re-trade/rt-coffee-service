package org.retrade.main.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;
import org.retrade.main.model.constant.PlatformConfigValueTypeEnum;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "platform_settings")
public class PlatformSettingEntity extends BaseSQLEntity {
    @Column(name = "key", nullable = false, length = 100, unique = true)
    private String key;
    @Column(name = "value", nullable = false, length = 255)
    private String value;
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PlatformConfigValueTypeEnum type;
}
