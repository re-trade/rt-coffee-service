package org.retrade.main.service.impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.main.service.FileService;
import org.retrade.main.util.AESEncryptUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final MinioClient minioClient;
    @Value("${minio.bucket-name}")
    private String BUCKET_NAME;
    @Value("${security.aes.key}")
    private String AES_KEY;

    @Override
    public String uploadEncrypted(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

            SecretKey secretKey = AESEncryptUtil.getKeyFromString(AES_KEY);
            InputStream encryptedStream = AESEncryptUtil.encryptStream(file.getInputStream(), secretKey);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(fileName)
                            .stream(encryptedStream, -1, 5 * 1024 * 1024)
                            .contentType(file.getContentType())
                            .build()
            );
            return fileName;
        } catch (Exception e) {
            throw new ActionFailedException("Upload failed: " + e.getMessage(), e);
        }
    }
}
