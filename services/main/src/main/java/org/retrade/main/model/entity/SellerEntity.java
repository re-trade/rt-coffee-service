package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

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
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
    @Column(name = "business_type")
    private Integer businessType;
    @Column(name = "avatar_url", length = 256)
    private String avatarUrl;
    @Column(name = "tax_code", length = 50)
    private String taxCode;
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
    @OneToOne(fetch = FetchType.EAGER, optional = false, targetEntity = AccountEntity.class)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;
}
