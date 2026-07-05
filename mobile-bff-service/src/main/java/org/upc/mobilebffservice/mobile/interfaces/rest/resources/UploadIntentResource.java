package org.upc.mobilebffservice.mobile.interfaces.rest.resources;

import org.upc.mobilebffservice.mobile.domain.model.valueobjects.UploadIntentStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UploadIntentResource(
        UUID uploadIntentId,
        UUID clientEvidenceId,
        String objectKey,
        String uploadUrl,
        String httpMethod,
        Map<String, String> requiredHeaders,
        Instant expiresAt,
        UploadIntentStatus status
) {
}
