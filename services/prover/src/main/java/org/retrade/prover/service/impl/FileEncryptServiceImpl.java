package org.retrade.prover.service.impl;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.prover.service.FileEncryptService;
import org.retrade.prover.util.AESEncryptUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileEncryptServiceImpl implements FileEncryptService {
    private final MinioClient minioClient;
    @Value("${security.aes.key}")
    private String AES_KEY;
    @Value( "${minio.bucket-name}")
    private String bucketName;
    @Override
    public File downloadEncryptedFileWithUrl(String fileName) {
        try {
            var encryptedStream = minioClient.getObject(GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                    .build());
            SecretKey secretKey = AESEncryptUtil.getKeyFromBytes(AES_KEY.getBytes());
            InputStream decryptedStream = AESEncryptUtil.decryptStream(encryptedStream, secretKey);
            File tempFile = File.createTempFile("decrypted-", "-" + fileName);
            tempFile.deleteOnExit();
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = decryptedStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }
            return tempFile;
        }  catch (ServerException | InsufficientDataException | ErrorResponseException | InternalException |
                  IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException |
                  XmlParserException e) {
            throw new RuntimeException("Failed to download or decrypt file: " + e.getMessage(), e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
