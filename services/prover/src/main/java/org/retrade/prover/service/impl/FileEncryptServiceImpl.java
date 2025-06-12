package org.retrade.prover.service.impl;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.MinioException;
import jakarta.validation.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.prover.service.FileEncryptService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileEncryptServiceImpl implements FileEncryptService {
    private final MinioClient minioClient;
    @Value( "${minio.bucket-name}")
    private String bucketName;
    @Override
    public File downloadEncryptedFileWithUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            System.err.println("File URL cannot be null or empty.");
            return null;
        }
        String objectName;
        try {
            String urlWithoutScheme = fileUrl.replaceFirst("^(http|https)://[^/]+/", "");
            if (!urlWithoutScheme.startsWith(bucketName + "/")) {
                log.error("URL does not seem to belong to the configured bucket: {}", bucketName);
                return null;
            }
            objectName = urlWithoutScheme.substring(bucketName.length() + 1);

            if (objectName.isEmpty()) {
                log.error("URL does not contain an object name.");
                return null;
            }

        } catch (Exception e) {
            System.err.println("Error parsing file URL: " + fileUrl + " - " + e.getMessage());
            return null;
        }
        File tempFile = null;
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {
            String fileExtension = "";
            int dotIndex = objectName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < objectName.length() - 1) {
                fileExtension = objectName.substring(dotIndex);
            }
            Path tempFilePath = (Path) Files.createTempFile("minio-download-" + UUID.randomUUID().toString(), fileExtension);
            tempFile = new File(tempFilePath.toString());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (InsufficientDataException e) {
            throw new RuntimeException(e);
        } catch (MinioException e) {
            log.error("MinIO error while downloading file '{}': {}", objectName, e.getMessage());
        } catch (IOException e) {
            log.error("I/O error while downloading or saving file '{}': {}", objectName, e.getMessage());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Security error while downloading file '{}': {}", objectName, e.getMessage());
        }
        return null;
    }
}
