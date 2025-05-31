package org.retrade.storage.service;

import org.retrade.storage.model.constant.StreamStatus;
import org.retrade.storage.model.entity.VideoStreamEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;

public interface VideoStreamService {
    
    VideoStreamEntity createVideoStream(VideoStreamEntity videoStream);
    
    VideoStreamEntity updateVideoStream(String streamId, VideoStreamEntity videoStream);
    
    VideoStreamEntity getVideoStreamById(String streamId);

    
    Page<VideoStreamEntity> getVideoStreams(Pageable pageable);
    
    Page<VideoStreamEntity> getVideoStreamsBySourceService(String sourceService, Pageable pageable);
    
    Page<VideoStreamEntity> getVideoStreamsByStatus(StreamStatus status, Pageable pageable);
    
    Page<VideoStreamEntity> getVideoStreamsByOwnerId(String ownerId, Pageable pageable);

    void processVideoStream(String streamId, InputStream videoData);
    
    void updateStreamStatus(String streamId, StreamStatus status);
    
    VideoStreamEntity updateStreamProgress(String streamId, Integer progress);
    
    void deleteVideoStream(String streamId);
    
    boolean existsById(String streamId);
    
    long countByStatus(StreamStatus status);
    
    long countBySourceService(String sourceService);
}
