package org.upc.mobilebffservice.mobile.interfaces.rest.resources;

import java.util.UUID;

public record CreateUploadIntentResource(
        UUID clientEvidenceId,
        Long driverId,
        Long orderId,
        Long routeId,
        String type,
        String mimeType,
        Long sizeBytes,
        String sha256
) {
}
