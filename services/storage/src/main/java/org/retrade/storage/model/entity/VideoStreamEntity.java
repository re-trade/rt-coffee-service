package org.retrade.storage.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.retrade.common.model.entity.BaseSQLEntity;
import org.retrade.storage.model.constant.StreamStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "video_streams")
public class VideoStreamEntity extends BaseSQLEntity {
    
    @Column(name = "source_service", nullable = false, length = 100)
    private String sourceService;
    
    @Column(name = "stream_url", columnDefinition = "TEXT")
    private String streamUrl;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StreamStatus status;
    
    @Column(name = "duration_seconds")
    private Long durationSeconds;
    
    @Column(name = "resolution", length = 20)
    private String resolution;
    
    @Column(name = "bitrate")
    private Integer bitrate;
    
    @Column(name = "format", length = 20)
    private String format;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "stored_file_url", columnDefinition = "TEXT")
    private String storedFileUrl;
    
    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;
    
    @Column(name = "owner_id", length = 36)
    private String ownerId;
    
    @Column(name = "processing_progress")
    private Integer processingProgress;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
