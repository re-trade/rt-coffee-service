package org.retrade.storage.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.provider.aws.model.S3FileRequest;
import org.retrade.provider.aws.model.S3FileResponse;
import org.retrade.provider.aws.s3.S3FileHandler;
import org.retrade.provider.aws.util.FileUtils;
import org.retrade.storage.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final S3FileHandler s3FileHandler;

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
            return s3FileHandler.upload(S3FileRequest.builder()
                    .file(file.getBytes())
                    .fileName(fileName)
                    .build());
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
    public File downloadFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new ValidationException("File URL cannot be empty");
        }
        try {
            return s3FileHandler.downloadFile(fileUrl);
        } catch (Exception e) {
            throw new ValidationException("Failed to download file: " + e.getMessage());
        }
    }
}
