package org.upc.mobilebffservice.mobile.infrastructure.storage;

import java.util.Map;

public record UploadedObjectMetadata(
        long contentLength,
        String contentType,
        Map<String, String> metadata
) {
}
