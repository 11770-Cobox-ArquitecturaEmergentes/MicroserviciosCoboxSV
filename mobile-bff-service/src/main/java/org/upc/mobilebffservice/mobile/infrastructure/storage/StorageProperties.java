package org.upc.mobilebffservice.mobile.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.s3")
public record StorageProperties(
        String bucket,
        String region,
        Long presignedUrlExpirationMinutes
) {
    public long expirationMinutes() {
        return presignedUrlExpirationMinutes == null ? 15L : presignedUrlExpirationMinutes;
    }
}
