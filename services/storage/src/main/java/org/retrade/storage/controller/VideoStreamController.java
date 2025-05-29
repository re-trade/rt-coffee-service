package org.retrade.storage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.dto.response.ResponseObject;
import org.retrade.storage.model.constant.StreamStatus;
import org.retrade.storage.model.entity.VideoStreamEntity;
import org.retrade.storage.service.VideoStreamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("video-streams")
@Tag(name = "Video Stream Management", description = "APIs for managing video streams")
public class VideoStreamController {
    
    private final VideoStreamService videoStreamService;
    
    @PostMapping
    @Operation(summary = "Create a new video stream")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<VideoStreamEntity>> createVideoStream(
            @RequestBody VideoStreamEntity videoStream) {
        
        VideoStreamEntity createdStream = videoStreamService.createVideoStream(videoStream);
        
        return ResponseEntity.ok(new ResponseObject.Builder<VideoStreamEntity>()
                .success(true)
                .code("SUCCESS")
                .content(createdStream)
                .messages("Video stream created successfully")
                .build());
    }
    
    @GetMapping("{streamId}")
    @Operation(summary = "Get video stream by ID")
    public ResponseEntity<ResponseObject<VideoStreamEntity>> getVideoStream(
            @Parameter(description = "Stream ID") @PathVariable String streamId) {
        
        VideoStreamEntity videoStream = videoStreamService.getVideoStreamById(streamId);
        
        return ResponseEntity.ok(new ResponseObject.Builder<VideoStreamEntity>()
                .success(true)
                .code("SUCCESS")
                .content(videoStream)
                .messages("Video stream retrieved successfully")
                .build());
    }
    
    @PutMapping("{streamId}")
    @Operation(summary = "Update video stream")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<VideoStreamEntity>> updateVideoStream(
            @Parameter(description = "Stream ID") @PathVariable String streamId,
            @RequestBody VideoStreamEntity videoStream) {
        
        VideoStreamEntity updatedStream = videoStreamService.updateVideoStream(streamId, videoStream);
        
        return ResponseEntity.ok(new ResponseObject.Builder<VideoStreamEntity>()
                .success(true)
                .code("SUCCESS")
                .content(updatedStream)
                .messages("Video stream updated successfully")
                .build());
    }
    
    @DeleteMapping("{streamId}")
    @Operation(summary = "Delete video stream")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject<String>> deleteVideoStream(
            @Parameter(description = "Stream ID") @PathVariable String streamId) {
        
        videoStreamService.deleteVideoStream(streamId);
        
        return ResponseEntity.ok(new ResponseObject.Builder<String>()
                .success(true)
                .code("SUCCESS")
                .content("Video stream deleted")
                .messages("Video stream deleted successfully")
                .build());
    }
    
    @GetMapping
    @Operation(summary = "Get all video streams with pagination")
    public ResponseEntity<ResponseObject<Page<VideoStreamEntity>>> getVideoStreams(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoStreamEntity> videoStreams = videoStreamService.getVideoStreams(pageable);
        
        return ResponseEntity.ok(new ResponseObject.Builder<Page<VideoStreamEntity>>()
                .success(true)
                .code("SUCCESS")
                .content(videoStreams)
                .messages("Video streams retrieved successfully")
                .build());
    }
    
    @GetMapping("by-source/{sourceService}")
    @Operation(summary = "Get video streams by source service")
    public ResponseEntity<ResponseObject<Page<VideoStreamEntity>>> getVideoStreamsBySourceService(
            @Parameter(description = "Source service name") @PathVariable String sourceService,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoStreamEntity> videoStreams = videoStreamService.getVideoStreamsBySourceService(sourceService, pageable);
        
        return ResponseEntity.ok(new ResponseObject.Builder<Page<VideoStreamEntity>>()
                .success(true)
                .code("SUCCESS")
                .content(videoStreams)
                .messages("Video streams retrieved successfully")
                .build());
    }
    
    @GetMapping("/by-status/{status}")
    @Operation(summary = "Get video streams by status")
    public ResponseEntity<ResponseObject<Page<VideoStreamEntity>>> getVideoStreamsByStatus(
            @Parameter(description = "Stream status") @PathVariable StreamStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoStreamEntity> videoStreams = videoStreamService.getVideoStreamsByStatus(status, pageable);
        
        return ResponseEntity.ok(new ResponseObject.Builder<Page<VideoStreamEntity>>()
                .success(true)
                .code("SUCCESS")
                .content(videoStreams)
                .messages("Video streams retrieved successfully")
                .build());
    }
    
    @GetMapping("/by-owner/{ownerId}")
    @Operation(summary = "Get video streams by owner ID")
    public ResponseEntity<ResponseObject<Page<VideoStreamEntity>>> getVideoStreamsByOwnerId(
            @Parameter(description = "Owner ID") @PathVariable String ownerId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoStreamEntity> videoStreams = videoStreamService.getVideoStreamsByOwnerId(ownerId, pageable);
        
        return ResponseEntity.ok(new ResponseObject.Builder<Page<VideoStreamEntity>>()
                .success(true)
                .code("SUCCESS")
                .content(videoStreams)
                .messages("Video streams retrieved successfully")
                .build());
    }
    
    @PatchMapping("{streamId}/progress")
    @Operation(summary = "Update video stream processing progress")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SELLER')")
    public ResponseEntity<ResponseObject<VideoStreamEntity>> updateStreamProgress(
            @Parameter(description = "Stream ID") @PathVariable String streamId,
            @Parameter(description = "Progress percentage") @RequestParam Integer progress) {
        
        VideoStreamEntity updatedStream = videoStreamService.updateStreamProgress(streamId, progress);
        
        return ResponseEntity.ok(new ResponseObject.Builder<VideoStreamEntity>()
                .success(true)
                .code("SUCCESS")
                .content(updatedStream)
                .messages("Stream progress updated successfully")
                .build());
    }
    
    @GetMapping("stats/count-by-source/{sourceService}")
    @Operation(summary = "Get count of streams by source service")
    public ResponseEntity<ResponseObject<Long>> getCountBySourceService(
            @Parameter(description = "Source service name") @PathVariable String sourceService) {
        
        long count = videoStreamService.countBySourceService(sourceService);
        
        return ResponseEntity.ok(new ResponseObject.Builder<Long>()
                .success(true)
                .code("SUCCESS")
                .content(count)
                .messages("Stream count retrieved successfully")
                .build());
    }
}
