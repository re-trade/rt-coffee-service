package org.retrade.storage.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "media_files")
public class MediaFileEntity extends BaseSQLEntity {
    
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;
    
    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;
    
    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;
    
    @Column(name = "download_count")
    private Long downloadCount;
}
