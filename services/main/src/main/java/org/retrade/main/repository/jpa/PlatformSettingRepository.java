package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.PlatformSettingEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatformSettingRepository extends BaseJpaRepository<PlatformSettingEntity, String> {
    Optional<PlatformSettingEntity> findByKey(String key);
}
