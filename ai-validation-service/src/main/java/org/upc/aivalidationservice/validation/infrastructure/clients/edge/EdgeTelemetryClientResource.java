package org.upc.aivalidationservice.validation.infrastructure.clients.edge;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EdgeTelemetryClientResource(
        UUID clientTelemetryId,
        Long routeId,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal accuracyMeters,
        BigDecimal speedKmh,
        Integer batteryLevel,
        Instant capturedAt
) {
}
