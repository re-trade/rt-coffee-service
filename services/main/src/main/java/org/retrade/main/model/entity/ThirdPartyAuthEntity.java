package org.retrade.main.model.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.retrade.common.model.entity.BaseSQLEntity;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "third_party_authentications")
public class ThirdPartyAuthEntity extends BaseSQLEntity {
    private String provider;
    private String providerId;
    private String providerEmail;
}