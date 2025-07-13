package org.retrade.main.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;
import org.retrade.main.model.constant.WithdrawStatusEnum;

import java.math.BigDecimal;
import java.sql.Timestamp;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "withdraw_requests")
public class WithdrawRequestEntity extends BaseSQLEntity {
    @ManyToOne(targetEntity = AccountEntity.class)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    @Column(name = "status", nullable = false)
    private WithdrawStatusEnum status;
    @Column(name = "processed_date")
    private Timestamp processedDate;
    @Column(name = "bank_account", length = 20, nullable = false)
    private String bankAccount;
    @Column(name = "bank_bin", length = 10, nullable = false)
    private String bankBin;
    @Column(name = "user_bank_name", length = 128, nullable = false)
    private String userBankName;
    @Column(name = "qr_code_url", columnDefinition = "TEXT")
    private String qrCodeUrl;
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    @Column(name = "prove_image_url", length = 255)
    private String proveImageUrl;
}
