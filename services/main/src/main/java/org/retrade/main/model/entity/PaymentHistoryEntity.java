package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;
import org.retrade.main.model.constant.PaymentStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "payment_histories")
public class PaymentHistoryEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = OrderEntity.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private OrderEntity order;
    @ManyToOne(targetEntity = PaymentMethodEntity.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethodEntity paymentMethod;
    @Column(name = "payment_total", nullable = false)
    private BigDecimal paymentTotal;
    @Column(name = "payment_content", length = 1024)
    private String paymentContent;
    @Column(name = "payment_code", length = 20)
    private String paymentCode;
    @Column(name = "payment_status", nullable = false, columnDefinition = "VARCHAR DEFAULT 'CREATED'")
    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum paymentStatus;
    @Column(name = "payment_time")
    private LocalDateTime paymentTime;
}
