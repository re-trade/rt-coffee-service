package org.retrade.storage.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.storage.model.entity.MediaFileEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaFileRepository extends BaseJpaRepository<MediaFileEntity, String> {
}
