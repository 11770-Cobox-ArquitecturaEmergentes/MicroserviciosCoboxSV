package org.upc.desktopbffservice.desktop.infrastructure.clients.aivalidation;

import java.time.Instant;

public record EvidencePreviewUrlClientResource(
        String previewUrl,
        Instant expiresAt
) {
}
