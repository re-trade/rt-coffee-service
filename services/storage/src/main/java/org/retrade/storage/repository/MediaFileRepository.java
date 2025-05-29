package org.retrade.storage.repository;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.storage.model.entity.MediaFileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaFileRepository extends BaseJpaRepository<MediaFileEntity, String> {
    
    Optional<MediaFileEntity> findByStoredName(String storedName);
    
    Optional<MediaFileEntity> findByFileUrl(String fileUrl);
    
    List<MediaFileEntity> findByOwnerId(String ownerId);
    
    Page<MediaFileEntity> findByOwnerId(String ownerId, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM media_files m WHERE m.ownerId = :ownerId")
    long countByOwnerId(@Param("ownerId") String ownerId);
    
    @Query("SELECT SUM(m.fileSize) FROM media_files m WHERE m.ownerId = :ownerId")
    Long getTotalFileSizeByOwnerId(@Param("ownerId") String ownerId);
}
