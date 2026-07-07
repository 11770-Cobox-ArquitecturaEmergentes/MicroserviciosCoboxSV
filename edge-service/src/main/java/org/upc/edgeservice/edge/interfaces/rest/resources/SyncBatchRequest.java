package org.upc.edgeservice.edge.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SyncBatchRequest(
        UUID clientBatchId,
        Long driverId,
        String deviceId,
        Instant sentAt,
        List<EdgeEvidenceRequest> evidences,
        List<EdgeTelemetryRequest> telemetry,
        List<EdgeEventRequest> events
) {
}
