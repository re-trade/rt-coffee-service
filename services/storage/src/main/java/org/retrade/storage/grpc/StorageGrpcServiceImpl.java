package org.retrade.storage.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.proto.storage.*;
import org.retrade.storage.model.constant.StreamStatus;
import org.retrade.storage.model.entity.VideoStreamEntity;
import org.retrade.storage.service.VideoStreamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.grpc.server.service.GrpcService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class StorageGrpcServiceImpl extends GrpcStorageServiceGrpc.GrpcStorageServiceImplBase {
    
    private final VideoStreamService videoStreamService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private final ConcurrentMap<String, ByteArrayOutputStream> activeStreams = new ConcurrentHashMap<>();
    
    @Override
    public StreamObserver<StreamVideoRequest> streamVideo(StreamObserver<StreamVideoResponse> responseObserver) {
        return new StreamObserver<>() {
            private String streamId;
            private VideoStreamEntity videoStream;
            
            @Override
            public void onNext(StreamVideoRequest request) {
                try {
                    if (request.hasStreamInfo()) {
                        handleStreamInfo(request.getStreamInfo());
                    } else if (request.hasChunk()) {
                        handleVideoChunk(request.getChunk());
                    }
                } catch (Exception e) {
                    log.error("Error processing stream request: {}", e.getMessage(), e);
                    responseObserver.onError(e);
                }
            }
            
            @Override
            public void onError(Throwable t) {
                log.error("Stream error for stream {}: {}", streamId, t.getMessage(), t);
                if (streamId != null) {
                    activeStreams.remove(streamId);
                    if (videoStream != null) {
                        videoStreamService.updateStreamStatus(streamId, StreamStatus.FAILED);
                    }
                }
            }
            
            @Override
            public void onCompleted() {
                try {
                    if (streamId != null && activeStreams.containsKey(streamId)) {
                        ByteArrayOutputStream outputStream = activeStreams.remove(streamId);
                        byte[] videoData = outputStream.toByteArray();
                        
                        videoStreamService.processVideoStream(streamId, new ByteArrayInputStream(videoData));
                        
                        StreamVideoResponse response = StreamVideoResponse.newBuilder()
                                .setSuccess(true)
                                .setMessage("Video stream processed successfully")
                                .setStreamId(streamId)
                                .setStatus(org.retrade.proto.storage.StreamStatus.COMPLETED)
                                .build();
                        
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                        
                        log.info("Video stream completed successfully: {}", streamId);
                    }
                } catch (Exception e) {
                    log.error("Error completing stream: {}", e.getMessage(), e);
                    responseObserver.onError(e);
                }
            }
            
            private void handleStreamInfo(VideoStreamInfo streamInfo) {
                VideoStreamEntity entity = VideoStreamEntity.builder()
                        .sourceService(streamInfo.getSourceService())
                        .streamUrl(streamInfo.getStreamUrl())
                        .title(streamInfo.getTitle())
                        .description(streamInfo.getDescription())
                        .status(mapStreamStatus(streamInfo.getStatus()))
                        .durationSeconds(streamInfo.getDurationSeconds())
                        .resolution(streamInfo.getResolution())
                        .bitrate(streamInfo.getBitrate())
                        .format(streamInfo.getFormat())
                        .build();
                
                videoStream = videoStreamService.createVideoStream(entity);
                streamId = videoStream.getId();
                activeStreams.put(streamId, new ByteArrayOutputStream());
                
                log.info("Created video stream: {} from service: {}", streamId, streamInfo.getSourceService());
            }
            
            private void handleVideoChunk(VideoStreamChunk chunk) throws IOException {
                if (streamId == null || !activeStreams.containsKey(streamId)) {
                    throw new IllegalStateException("Stream not initialized");
                }
                
                ByteArrayOutputStream outputStream = activeStreams.get(streamId);
                outputStream.write(chunk.getData().toByteArray());
                
                int progress = chunk.getSequenceNumber() * 10;
                if (progress <= 100) {
                    videoStreamService.updateStreamProgress(streamId, progress);
                }
                
                log.debug("Received chunk {} for stream {}", chunk.getSequenceNumber(), streamId);
            }
        };
    }

    @Override
    public void getVideoStream(GetVideoStreamRequest request, StreamObserver<GetVideoStreamResponse> responseObserver) {
        try {
            log.info("Getting video stream: {}", request.getStreamId());
            
            VideoStreamEntity videoStream = videoStreamService.getVideoStreamById(request.getStreamId());
            VideoStreamInfo streamInfo = mapToVideoStreamInfo(videoStream);
            
            GetVideoStreamResponse response = GetVideoStreamResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Video stream retrieved successfully")
                    .setStreamInfo(streamInfo)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Error getting video stream: {}", e.getMessage(), e);
            
            GetVideoStreamResponse response = GetVideoStreamResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to get video stream: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void listVideoStreams(ListVideoStreamsRequest request, StreamObserver<ListVideoStreamsResponse> responseObserver) {
        try {
            PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
            Page<VideoStreamEntity> streams;
            
            if (!request.getSourceService().isEmpty() && request.getStatus() != org.retrade.proto.storage.StreamStatus.STREAM_STATUS_UNSPECIFIED) {
                streams = videoStreamService.getVideoStreamsBySourceService(request.getSourceService(), pageRequest);
            } else if (!request.getSourceService().isEmpty()) {
                streams = videoStreamService.getVideoStreamsBySourceService(request.getSourceService(), pageRequest);
            } else if (request.getStatus() != org.retrade.proto.storage.StreamStatus.STREAM_STATUS_UNSPECIFIED) {
                streams = videoStreamService.getVideoStreamsByStatus(mapStreamStatus(request.getStatus()), pageRequest);
            } else {
                streams = videoStreamService.getVideoStreams(pageRequest);
            }
            
            ListVideoStreamsResponse.Builder responseBuilder = ListVideoStreamsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Video streams retrieved successfully")
                    .setTotalCount((int) streams.getTotalElements());
            
            streams.getContent().forEach(stream -> {
                responseBuilder.addStreams(mapToVideoStreamInfo(stream));
            });
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Error listing video streams: {}", e.getMessage(), e);
            
            ListVideoStreamsResponse response = ListVideoStreamsResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to list video streams: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private VideoStreamInfo mapToVideoStreamInfo(VideoStreamEntity entity) {
        return VideoStreamInfo.newBuilder()
                .setStreamId(entity.getId())
                .setSourceService(entity.getSourceService() != null ? entity.getSourceService() : "")
                .setStreamUrl(entity.getStreamUrl() != null ? entity.getStreamUrl() : "")
                .setTitle(entity.getTitle() != null ? entity.getTitle() : "")
                .setDescription(entity.getDescription() != null ? entity.getDescription() : "")
                .setStatus(mapStreamStatusToProto(entity.getStatus()))
                .setDurationSeconds(entity.getDurationSeconds() != null ? entity.getDurationSeconds() : 0)
                .setResolution(entity.getResolution() != null ? entity.getResolution() : "")
                .setBitrate(entity.getBitrate() != null ? entity.getBitrate() : 0)
                .setFormat(entity.getFormat() != null ? entity.getFormat() : "")
                .setCreatedAt(entity.getCreatedDate() != null ? entity.getCreatedDate().toLocalDateTime().format(DATE_FORMATTER) : "")
                .setUpdatedAt(entity.getUpdatedDate() != null ? entity.getUpdatedDate().toLocalDateTime().format(DATE_FORMATTER) : "")
                .build();
    }

    private StreamStatus mapStreamStatus(org.retrade.proto.storage.StreamStatus status) {
        return switch (status) {
            case ACTIVE -> StreamStatus.ACTIVE;
            case PROCESSING -> StreamStatus.PROCESSING;
            case COMPLETED -> StreamStatus.COMPLETED;
            case FAILED -> StreamStatus.FAILED;
            default -> StreamStatus.INACTIVE;
        };
    }
    
    private org.retrade.proto.storage.StreamStatus mapStreamStatusToProto(StreamStatus status) {
        return switch (status) {
            case ACTIVE -> org.retrade.proto.storage.StreamStatus.ACTIVE;
            case INACTIVE -> org.retrade.proto.storage.StreamStatus.INACTIVE;
            case PROCESSING -> org.retrade.proto.storage.StreamStatus.PROCESSING;
            case COMPLETED -> org.retrade.proto.storage.StreamStatus.COMPLETED;
            case FAILED -> org.retrade.proto.storage.StreamStatus.FAILED;
        };
    }
}
