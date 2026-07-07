package org.upc.edgeservice.edge.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EdgeTelemetryResource(
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
