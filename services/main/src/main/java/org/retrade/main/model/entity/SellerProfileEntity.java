package org.retrade.main.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "seller_profiles")
public class SellerProfileEntity extends BaseSQLEntity {
    @Column(name = "owner_name", length = 255)
    private String ownerName;
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    @Column(name = "avatar_url", length = 255, nullable = false)
    private String avatarUrl;
    @Column(name = "background", length = 255, nullable = false)
    private String background;
    @Column(name = "business_name", length = 255)
    private String businessName;
    @Column(name = "business_license", length = 100)
    private String businessLicense;
    @Column(name = "tax_code", length = 50)
    private String taxCode;
    @Column(name = "email", length = 255)
    private String email;
    @Column(name = "website", length = 255)
    private String website;
    @Column(name = "country", length = 100)
    private String country;
    @Column(name = "state", length = 100)
    private String state;
    @Column(name = "city", length = 100)
    private String city;
    @Column(name = "district", length = 100)
    private String district;
    @Column(name = "ward", length = 100)
    private String ward;
    @Column(name = "street_address", columnDefinition = "TEXT")
    private String streetAddress;
    @Column(name = "status", length = 50)
    private String status;
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    @OneToOne(fetch = FetchType.EAGER, optional = false, targetEntity = AccountEntity.class)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

}
