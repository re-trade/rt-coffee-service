package org.retrade.main.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "customer_bank_infos")
public class CustomerBankInfoEntity extends BaseSQLEntity {
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = CustomerEntity.class, optional = false)
    @JoinColumn(name = "customer_id",  nullable = false)
    private CustomerEntity customer;
    @Column(name = "bank_name", length = 128, nullable = false)
    private String bankName;
    @Column(name = "bank_code", length = 20, nullable = false)
    private String bankCode;
    @Column(name = "account_number", length = 20, nullable = false)
    private String accountNumber;
    @Column(name = "user_bank_name", length = 128)
    private String userBankName;
}
