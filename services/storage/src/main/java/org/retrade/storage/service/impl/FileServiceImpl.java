package org.retrade.storage.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.provider.aws.model.S3FileRequest;
import org.retrade.provider.aws.model.S3FileResponse;
import org.retrade.provider.aws.s3.S3FileHandler;
import org.retrade.provider.aws.util.FileUtils;
import org.retrade.storage.model.constant.FileType;
import org.retrade.storage.model.dto.FileUploadResponse;
import org.retrade.storage.model.entity.MediaFileEntity;
import org.retrade.storage.repository.MediaFileRepository;
import org.retrade.storage.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
            return s3FileHandler.uploadBulkFile(fileMapper);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Failed to upload files: " + e.getMessage());
        }
    }

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, FileType fileType) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File cannot be empty");
        }
        try {
            return uploadFile(file.getBytes(), file.getOriginalFilename(), file.getContentType(), fileType);
        } catch (Exception e) {
            throw new ValidationException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public FileUploadResponse uploadFile(byte[] fileData, String originalName, String mimeType, FileType fileType) {
        if (fileData == null || fileData.length == 0) {
            throw new ValidationException("File data cannot be empty");
        }

        if (originalName == null || originalName.trim().isEmpty()) {
            throw new ValidationException("Original file name is required");
        }

        if (fileType == null) {
            throw new ValidationException("File type is required");
        }

        try {
            if (!FileUtils.verifyFile(fileData)) {
                throw new ValidationException("Invalid file type");
            }

            String fileName = FileUtils.generateFileName(originalName);
            String checksum = calculateChecksum(fileData);

            S3FileResponse response = s3FileHandler.upload(S3FileRequest.builder()
                    .file(fileData)
                    .fileName(fileName)
                    .build());

            log.info("Uploaded file: {} with type: {}", originalName, fileType);

            return FileUploadResponse.builder()
                    .fileName(fileName)
                    .originalName(originalName)
                    .fileUrl(response.getFileUrl())
                    .mimeType(mimeType != null ? mimeType : "application/octet-stream")
                    .fileSize((long) fileData.length)
                    .checksum(checksum)
                    .fileType(fileType)
                    .build();

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public FileUploadResponse uploadAvatar(MultipartFile file) {
        return uploadFile(file, FileType.AVATAR);
    }

    @Override
    public FileUploadResponse uploadImage(MultipartFile file) {
        return uploadFile(file, FileType.IMAGE);
    }

    @Override
    public FileUploadResponse uploadDocument(MultipartFile file) {
        return uploadFile(file, FileType.DOCUMENT);
    }

    public MediaFileEntity saveMediaFile(FileUploadResponse fileUploadResponse, String ownerId) {
        if (fileUploadResponse == null) {
            throw new ValidationException("File upload response cannot be null");
        }

        try {
            MediaFileEntity mediaFile = MediaFileEntity.builder()
                    .originalName(fileUploadResponse.getOriginalName())
                    .storedName(fileUploadResponse.getFileName())
                    .fileUrl(fileUploadResponse.getFileUrl())
                    .fileSize(fileUploadResponse.getFileSize())
                    .mimeType(fileUploadResponse.getMimeType())
                    .ownerId(ownerId)
                    .isPublic(false)
                    .downloadCount(0L)
                    .checksum(fileUploadResponse.getChecksum())
                    .build();

            MediaFileEntity savedEntity = mediaFileRepository.save(mediaFile);
            log.info("Saved media file to database: {} with ID: {}", fileUploadResponse.getOriginalName(), savedEntity.getId());
            return savedEntity;

        } catch (Exception e) {
            log.error("Failed to save media file to database: {}", fileUploadResponse.getOriginalName(), e);
            throw new ValidationException("Failed to save media file to database: " + e.getMessage());
        }
    }

    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.warn("SHA-256 algorithm not available, skipping checksum calculation");
            return null;
        }
    }
}
