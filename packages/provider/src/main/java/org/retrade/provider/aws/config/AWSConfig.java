package org.retrade.provider.aws.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class AWSConfig {
    private final AWSConfigValue awsConfigValue;
    @Bean
    S3Client s3Client () {
        return S3Client.builder()
                .region(Region.of(awsConfigValue.getRegion()))
                .endpointOverride(URI.create(String.format("https://%s",awsConfigValue.getEndpoint())))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsConfigValue.getAccessKey(), awsConfigValue.getSecretKey())))
                .build();
    }
    @Bean
    S3AsyncClient s3AsyncClient () {
        return S3AsyncClient.builder()
                .region(Region.of(awsConfigValue.getRegion()))
                .endpointOverride(URI.create(String.format("https://%s",awsConfigValue.getEndpoint())))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsConfigValue.getAccessKey(), awsConfigValue.getSecretKey())))
                .build();
    }
}
