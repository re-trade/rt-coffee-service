package org.retrade.storage.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.storage.model.constant.StreamStatus;
import org.retrade.storage.model.entity.VideoStreamEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoStreamRepository extends BaseJpaRepository<VideoStreamEntity, String> {
    
    Page<VideoStreamEntity> findBySourceService(String sourceService, Pageable pageable);
    
    Page<VideoStreamEntity> findByStatus(StreamStatus status, Pageable pageable);
    
    Page<VideoStreamEntity> findByOwnerId(String ownerId, Pageable pageable);

    @Query("SELECT COUNT(v) FROM video_streams v WHERE v.status = :status")
    long countByStatus(@Param("status") StreamStatus status);
    
    @Query("SELECT COUNT(v) FROM video_streams v WHERE v.sourceService = :sourceService")
    long countBySourceService(@Param("sourceService") String sourceService);
}
