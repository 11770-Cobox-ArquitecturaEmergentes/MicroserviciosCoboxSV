package org.upc.aivalidationservice.validation.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.s3")
public record StorageProperties(
        String bucket,
        String region
) {
    public StorageProperties {
        bucket = bucket == null ? "coboxsv-evidence-prod" : bucket;
        region = region == null ? "us-east-1" : region;
    }
}
