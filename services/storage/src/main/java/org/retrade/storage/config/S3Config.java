package org.retrade.storage.config;

import lombok.RequiredArgsConstructor;
import org.retrade.provider.aws.config.AWSConfigValue;
import org.retrade.provider.aws.s3.S3FileHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ComponentScan(basePackageClasses = {AWSConfigValue.class})
@RequiredArgsConstructor
public class S3Config {
    private final AWSConfigValue awsConfigValue;
    private final S3Client awsClient;
    private final S3AsyncClient s3AsyncClient;
    @Bean
    public S3FileHandler fileHandler() {
        return new S3FileHandler(awsClient, s3AsyncClient, awsConfigValue);
    }
}
