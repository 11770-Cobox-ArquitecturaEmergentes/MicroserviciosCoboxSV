package org.upc.aivalidationservice.shared.interfaces.rest.resources;

import java.time.Instant;

public record ErrorResponseResource(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
