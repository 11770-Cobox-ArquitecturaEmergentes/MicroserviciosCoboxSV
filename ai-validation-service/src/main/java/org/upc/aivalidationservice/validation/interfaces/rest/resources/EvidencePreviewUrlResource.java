package org.upc.aivalidationservice.validation.interfaces.rest.resources;

import java.time.Instant;

public record EvidencePreviewUrlResource(
        String previewUrl,
        Instant expiresAt
) {
}
