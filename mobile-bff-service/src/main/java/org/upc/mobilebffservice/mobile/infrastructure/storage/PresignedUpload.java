package org.upc.mobilebffservice.mobile.infrastructure.storage;

import java.net.URL;
import java.time.Instant;
import java.util.Map;

public record PresignedUpload(
        URL uploadUrl,
        String httpMethod,
        Map<String, String> requiredHeaders,
        Instant expiresAt
) {
}
