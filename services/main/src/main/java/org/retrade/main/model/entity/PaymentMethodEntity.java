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
@Entity(name = "payment_methods")
public class PaymentMethodEntity extends BaseSQLEntity {
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;
    @Column(name = "description", nullable = false, length = 500)
    private String description;
    @Column(name = "img_url", nullable = false)
    private String imgUrl;
    @Column(name = "type", nullable = false)
    private String type;
    @Column(name = "handler_class", nullable = false)
    private String handlerClass;
    @Column(name  = "callback_uri", nullable = false, unique = true)
    private String callbackUri;
    @Column(name = "enabled", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean enabled;
}
