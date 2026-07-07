package org.upc.aivalidationservice.validation.infrastructure.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.upc.aivalidationservice.validation.infrastructure.storage.StorageProperties;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.textract.TextractClient;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class AwsAiConfiguration {

    @Bean
    S3Client s3Client(StorageProperties properties) {
        return S3Client.builder()
                .region(Region.of(properties.region()))
                .build();
    }

    @Bean
    S3Presigner s3Presigner(StorageProperties properties) {
        return S3Presigner.builder()
                .region(Region.of(properties.region()))
                .build();
    }

    @Bean
    TextractClient textractClient(StorageProperties properties) {
        return TextractClient.builder()
                .region(Region.of(properties.region()))
                .build();
    }

    @Bean
    RekognitionClient rekognitionClient(StorageProperties properties) {
        return RekognitionClient.builder()
                .region(Region.of(properties.region()))
                .build();
    }
}
