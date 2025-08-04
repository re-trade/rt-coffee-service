package org.retrade.main.service.impl;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ActionFailedException;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.constant.IdentityCardTypeEnum;
import org.retrade.main.model.dto.response.DecodedFile;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.repository.jpa.SellerRepository;
import org.retrade.main.service.FileService;
import org.retrade.main.util.AESEncryptUtil;
import org.retrade.main.util.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final MinioClient minioClient;
    private final SellerRepository sellerRepository;
    private final AuthUtils authUtils;
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

    @Override
    public DecodedFile getSellerIdentityCard(String sellerId, IdentityCardTypeEnum type) {
        var roles = authUtils.getRolesFromAuthUser();
        if(!roles.contains("ROLE_ADMIN")) {
            throw new ValidationException("User does not have permission to get seller identity card");
        }
        var seller = sellerRepository.findById(sellerId).orElseThrow(() -> new ValidationException("Seller not found"));
        String fileUrl = getIdentityFileUrl(type, seller);
        var file = downloadEncryptedFileWithUrl(fileUrl);
        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = inputStream.readAllBytes();
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "image/jpeg";
            }
            return new DecodedFile(buffer, mimeType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download or decrypt file: " + e.getMessage(), e);
        }
    }

    private String getIdentityFileUrl(IdentityCardTypeEnum type, SellerEntity seller) {
        String fileUrl;
        switch (type) {
            case FRONT -> {
                if ("example".equals(seller.getFrontSideIdentityCard())) {
                    throw new ValidationException("Seller does not have front side identity card");
                }
                fileUrl = seller.getFrontSideIdentityCard();
            }
            case BACK -> {
                if ("example".equals(seller.getBackSideIdentityCard())) {
                    throw new ValidationException("Seller does not have back side identity card");
                }
                fileUrl = seller.getBackSideIdentityCard();
            }
            default -> throw new ValidationException("Unsupported identity card type");
        }
        return fileUrl;
    }

    private File downloadEncryptedFileWithUrl(String fileName) {
        try {
            var encryptedStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(fileName)
                    .build());
            SecretKey secretKey = AESEncryptUtil.getKeyFromString(AES_KEY);
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
