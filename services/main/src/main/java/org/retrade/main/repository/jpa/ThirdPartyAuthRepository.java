package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.ThirdPartyAuthEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThirdPartyAuthRepository extends BaseJpaRepository<ThirdPartyAuthEntity, String> {
    List<ThirdPartyAuthEntity> findByProvider(String provider);
    Optional<ThirdPartyAuthEntity> findByProviderAndProviderId(String provider, String providerId);
    Optional<ThirdPartyAuthEntity> findByProviderEmail(String providerEmail);
}
