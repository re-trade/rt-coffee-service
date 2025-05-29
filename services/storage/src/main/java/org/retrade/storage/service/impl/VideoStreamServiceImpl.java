package org.retrade.storage.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.provider.aws.model.S3FileRequest;
import org.retrade.provider.aws.model.S3FileResponse;
import org.retrade.provider.aws.s3.S3FileHandler;
import org.retrade.provider.aws.util.FileUtils;
import org.retrade.storage.model.constant.StreamStatus;
import org.retrade.storage.model.entity.VideoStreamEntity;
import org.retrade.storage.repository.VideoStreamRepository;
import org.retrade.storage.service.VideoStreamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoStreamServiceImpl implements VideoStreamService {
    
    private final VideoStreamRepository videoStreamRepository;
    private final S3FileHandler s3FileHandler;
    
    @Override
    @Transactional
    public VideoStreamEntity createVideoStream(VideoStreamEntity videoStream) {
        if (videoStream == null) {
            throw new ValidationException("Video stream cannot be null");
        }
        
        if (videoStream.getTitle() == null || videoStream.getTitle().trim().isEmpty()) {
            throw new ValidationException("Video stream title is required");
        }
        
        if (videoStream.getSourceService() == null || videoStream.getSourceService().trim().isEmpty()) {
            throw new ValidationException("Source service is required");
        }
        
        if (videoStream.getStatus() == null) {
            videoStream.setStatus(StreamStatus.INACTIVE);
        }
        
        log.info("Creating video stream: {}", videoStream.getTitle());
        return videoStreamRepository.save(videoStream);
    }
    
    @Override
    @Transactional
    public VideoStreamEntity updateVideoStream(String streamId, VideoStreamEntity videoStream) {
        if (streamId == null || streamId.trim().isEmpty()) {
            throw new ValidationException("Stream ID is required");
        }
        
        VideoStreamEntity existingStream = getVideoStreamById(streamId);
        
        if (videoStream.getTitle() != null) {
            existingStream.setTitle(videoStream.getTitle());
        }
        if (videoStream.getDescription() != null) {
            existingStream.setDescription(videoStream.getDescription());
        }
        if (videoStream.getStatus() != null) {
            existingStream.setStatus(videoStream.getStatus());
        }
        if (videoStream.getDurationSeconds() != null) {
            existingStream.setDurationSeconds(videoStream.getDurationSeconds());
        }
        if (videoStream.getResolution() != null) {
            existingStream.setResolution(videoStream.getResolution());
        }
        if (videoStream.getBitrate() != null) {
            existingStream.setBitrate(videoStream.getBitrate());
        }
        if (videoStream.getFormat() != null) {
            existingStream.setFormat(videoStream.getFormat());
        }
        
        log.info("Updating video stream: {}", streamId);
        return videoStreamRepository.save(existingStream);
    }
    
    @Override
    public VideoStreamEntity getVideoStreamById(String streamId) {
        if (streamId == null || streamId.trim().isEmpty()) {
            throw new ValidationException("Stream ID is required");
        }
        
        return videoStreamRepository.findById(streamId)
                .orElseThrow(() -> new ValidationException("Video stream not found with ID: " + streamId));
    }
    
    @Override
    public Page<VideoStreamEntity> getVideoStreams(Pageable pageable) {
        return videoStreamRepository.findAll(pageable);
    }
    
    @Override
    public Page<VideoStreamEntity> getVideoStreamsBySourceService(String sourceService, Pageable pageable) {
        if (sourceService == null || sourceService.trim().isEmpty()) {
            throw new ValidationException("Source service is required");
        }
        
        return videoStreamRepository.findBySourceService(sourceService, pageable);
    }
    
    @Override
    public Page<VideoStreamEntity> getVideoStreamsByStatus(StreamStatus status, Pageable pageable) {
        if (status == null) {
            throw new ValidationException("Status is required");
        }
        
        return videoStreamRepository.findByStatus(status, pageable);
    }
    
    @Override
    public Page<VideoStreamEntity> getVideoStreamsByOwnerId(String ownerId, Pageable pageable) {
        if (ownerId == null || ownerId.trim().isEmpty()) {
            throw new ValidationException("Owner ID is required");
        }
        
        return videoStreamRepository.findByOwnerId(ownerId, pageable);
    }
    
    @Override
    @Transactional
    public void processVideoStream(String streamId, InputStream videoData) {
        VideoStreamEntity stream = getVideoStreamById(streamId);
        
        try {
            stream.setStatus(StreamStatus.PROCESSING);
            videoStreamRepository.save(stream);
            
            byte[] data = videoData.readAllBytes();
            String fileName = FileUtils.generateFileName(stream.getTitle() + ".mp4");
            
            S3FileResponse response = s3FileHandler.upload(S3FileRequest.builder()
                    .file(data)
                    .fileName(fileName)
                    .build());
            
            stream.setStoredFileUrl(response.getFileUrl());
            stream.setFileSize((long) data.length);
            stream.setStatus(StreamStatus.COMPLETED);
            stream.setProcessingProgress(100);
            
            log.info("Video stream processed successfully: {}", streamId);
            videoStreamRepository.save(stream);
            
        } catch (IOException e) {
            stream.setStatus(StreamStatus.FAILED);
            stream.setErrorMessage("Failed to process video: " + e.getMessage());
            videoStreamRepository.save(stream);
            throw new ValidationException("Failed to process video stream: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void updateStreamStatus(String streamId, StreamStatus status) {
        VideoStreamEntity stream = getVideoStreamById(streamId);
        stream.setStatus(status);
        
        log.info("Updated stream status for {}: {}", streamId, status);
        videoStreamRepository.save(stream);
    }
    
    @Override
    @Transactional
    public VideoStreamEntity updateStreamProgress(String streamId, Integer progress) {
        VideoStreamEntity stream = getVideoStreamById(streamId);
        stream.setProcessingProgress(progress);
        
        return videoStreamRepository.save(stream);
    }
    
    @Override
    @Transactional
    public void deleteVideoStream(String streamId) {
        VideoStreamEntity stream = getVideoStreamById(streamId);
        
        log.info("Deleting video stream: {}", streamId);
        videoStreamRepository.delete(stream);
    }
    
    @Override
    public boolean existsById(String streamId) {
        return videoStreamRepository.existsById(streamId);
    }
    
    @Override
    public long countByStatus(StreamStatus status) {
        return videoStreamRepository.countByStatus(status);
    }
    
    @Override
    public long countBySourceService(String sourceService) {
        return videoStreamRepository.countBySourceService(sourceService);
    }
}
