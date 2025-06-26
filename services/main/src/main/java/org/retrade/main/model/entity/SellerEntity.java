package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;
import org.retrade.main.model.constant.IdentityVerifiedStatusEnum;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "sellers")
public class SellerEntity extends BaseSQLEntity {
    @Column(name = "shop_name", length = 50, nullable = false)
    private String shopName;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "avg_vote")
    private Double avgVote;
    @Column(name = "address_line", length = 100, nullable = false)
    private String addressLine;
    @Column(name = "district", length = 50, nullable = false)
    private String district;
    @Column(name = "ward", length = 50, nullable = false)
    private String ward;
    @Column(name = "state", length = 50, nullable = false)
    private String state;
    @Column(name = "avatar_url", length = 256)
    private String avatarUrl;
    @Column(name = "background", length = 256)
    private String background;
    @Column(name = "email", length = 50, nullable = false)
    private String email;
    @Column(name = "phone_number", length = 12, nullable = false)
    private String phoneNumber;
    @Column(name = "back_side_identity_card", length = 256)
    private String backSideIdentityCard;
    @Column(name = "front_side_identity_card", length = 256)
    private String frontSideIdentityCard;
    @Column(name = "identity_number", length = 20, nullable = false)
    private String identityNumber;
    @Column(name = "verified", nullable = false)
    private Boolean verified;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "identity_verified", nullable = false, columnDefinition = "SMALLINT DEFAULT 0")
    private IdentityVerifiedStatusEnum identityVerified;
    @Column(name = "balance", nullable = false, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
    private BigDecimal balance;
    @OneToOne(fetch = FetchType.EAGER, optional = false, targetEntity = AccountEntity.class)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;
}
