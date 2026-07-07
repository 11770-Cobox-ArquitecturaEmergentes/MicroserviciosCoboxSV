package org.upc.aivalidationservice.validation.application.internal.providers;

public record AiVisionRequest(
        String bucket,
        String objectKey,
        String mimeType
) {
}
