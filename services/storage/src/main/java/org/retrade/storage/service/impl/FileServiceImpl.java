package org.retrade.storage.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.provider.aws.model.S3FileRequest;
import org.retrade.provider.aws.model.S3FileResponse;
import org.retrade.provider.aws.s3.S3FileHandler;
import org.retrade.provider.aws.util.FileUtils;
import org.retrade.storage.model.entity.MediaFileEntity;
import org.retrade.storage.repository.MediaFileRepository;
import org.retrade.storage.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    private final S3FileHandler s3FileHandler;
    private final MediaFileRepository mediaFileRepository;

    @Override
    public S3FileResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File cannot be empty");
        }

        try {
            if (!FileUtils.verifyFile(file.getBytes())) {
                throw new ValidationException("Invalid file type. Only images (JPEG, PNG, WebP) and PDF files are allowed");
            }

            String fileName = FileUtils.generateFileName(file.getOriginalFilename());
            var result = s3FileHandler.upload(S3FileRequest.builder()
                    .file(file.getBytes())
                    .fileName(fileName)
                    .build());
            saveMediaFile(result);
            return result;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public Set<S3FileResponse> uploadBulkFile(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ValidationException("Files list cannot be empty");
        }
        try {
            var fileMapper = files.stream()
                    .map(file -> {
                        if (file == null || file.isEmpty()) {
                            throw new ValidationException("One or more files are empty");
                        }
                        try {
                            if (!FileUtils.verifyFile(file.getBytes())) {
                                throw new ValidationException("Invalid file type in bulk upload. Only images (JPEG, PNG, WebP) and PDF files are allowed");
                            }

                            String fileName = FileUtils.generateFileName(file.getOriginalFilename());
                            return S3FileRequest.builder()
                                    .file(file.getBytes())
                                    .fileName(fileName)
                                    .build();
                        } catch (Exception e) {
                            throw new ValidationException("Failed to process file: " + file.getOriginalFilename() + ". " + e.getMessage());
                        }
                    })
                    .collect(Collectors.toList());
            var fileS3Response =  s3FileHandler.uploadBulkFile(fileMapper);
            saveMediaFile(fileS3Response);
            return fileS3Response;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Failed to upload files: " + e.getMessage());
        }
    }

    private void saveMediaFile(Set<S3FileResponse> fileUploadResponse) {
        if (fileUploadResponse == null) {
            throw new ValidationException("File upload response cannot be null");
        }

        var mediaFiles = fileUploadResponse.stream().map(item -> MediaFileEntity.builder()
                .originalName(item.getFileName())
                .storedName(item.getFileName())
                .fileUrl(item.getFileUrl())
                .fileSize((long) item.getFile().length)
                .isPublic(true)
                .downloadCount(0L)
                .build()).collect(Collectors.toSet());

        try {
            mediaFileRepository.saveAllAndFlush(mediaFiles);
        } catch (Exception e) {
            throw new ValidationException("Failed to save media file to database: " + e.getMessage());
        }
    }

    private void saveMediaFile(S3FileResponse fileUploadResponse) {
        if (fileUploadResponse == null) {
            throw new ValidationException("File upload response cannot be null");
        }
        try {
            MediaFileEntity mediaFile = MediaFileEntity.builder()
                    .originalName(fileUploadResponse.getFileName())
                    .storedName(fileUploadResponse.getFileName())
                    .fileUrl(fileUploadResponse.getFileUrl())
                    .fileSize((long) fileUploadResponse.getFile().length)
                    .isPublic(true)
                    .downloadCount(0L)
                    .build();
            MediaFileEntity savedEntity = mediaFileRepository.save(mediaFile);
            log.info("Saved media file to database: {} with ID: {}", mediaFile.getOriginalName(), savedEntity.getId());

        } catch (Exception e) {
            log.error("Failed to save media file to database: {}", fileUploadResponse.getFileName(), e);
            throw new ValidationException("Failed to save media file to database: " + e.getMessage());
        }
    }
}
