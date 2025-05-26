package org.retrade.provider.aws.s3;

import org.retrade.provider.aws.config.AWSConfigValue;
import org.retrade.provider.aws.model.S3FileRequest;
import org.retrade.provider.aws.model.S3FileResponse;
import org.retrade.provider.aws.util.FileUtils;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class S3FileHandler {
    private final S3Client s3Client;
    private final S3AsyncClient s3AsyncClient;
    private final AWSConfigValue awsConfig;

    public S3FileHandler(S3Client s3Client, S3AsyncClient s3AsyncClient, AWSConfigValue awsConfig) {
        this.s3Client = s3Client;
        this.s3AsyncClient = s3AsyncClient;
        this.awsConfig = awsConfig;
    }

    public S3FileResponse upload(S3FileRequest file) {
        if(!FileUtils.verifyFile(file.getFile())) {
            throw new IllegalArgumentException("Invalid file");
        }
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsConfig.getBucket())
                    .key(file.getFileName())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(new ByteArrayInputStream(file.getFile()), file.getFile().length));
            return S3FileResponse.builder()
                    .fileName(file.getFileName())
                    .fileUrl(generatePublicUrl(file.getFileName()))
                    .file(file.getFile())
                    .build();
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public S3FileResponse upload(File file) {
        try {
            var fileName = FileUtils.generateFileName(file.getName());
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsConfig.getBucket())
                    .key(fileName)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
            return S3FileResponse.builder()
                    .fileName(fileName)
                    .fileUrl(generatePublicUrl(fileName))
                    .build();
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public File downloadFile(String fileUrl) {
        try {
            var uri = new URI(fileUrl);
            URL url = uri.toURL();
            Path tempFile = Files.createTempFile("temp", ".jpg");
            try (InputStream in = url.openStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFile.toFile();
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public Set<S3FileResponse> uploadBulkFile(List<S3FileRequest> files) {
        try (ExecutorService executorService = Executors.newFixedThreadPool(10)){
            return files.stream()
                    .map(file -> uploadFileAsync(file, executorService)
                            .exceptionally(ex -> {
                                System.err.println("Failed to upload file: " + file.getFileName());
                                return null;
                            }))
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
    }
    private String generatePublicUrl(String fileName) {
        return s3Client.utilities().getUrl(builder -> builder
                        .bucket(awsConfig.getBucket())
                        .key(fileName))
                .toString();
    }
    private CompletableFuture<S3FileResponse> uploadFileAsync(S3FileRequest file, ExecutorService executorService) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsConfig.getBucket())
                    .key(file.getFileName())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();
            return s3AsyncClient.putObject(putObjectRequest,
                            AsyncRequestBody.fromInputStream(new ByteArrayInputStream(file.getFile()), (long) file.getFile().length, executorService))
                    .thenApply(response -> S3FileResponse.builder()
                                .file(file.getFile())
                                .fileName(file.getFileName())
                                .fileUrl(generatePublicUrl(file.getFileName()))
                                .build());
        } catch (Exception ex) {
            CompletableFuture<S3FileResponse> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("Failed to upload file: " + file.getFileName(), ex));
            return failedFuture;
        }
    }
}
