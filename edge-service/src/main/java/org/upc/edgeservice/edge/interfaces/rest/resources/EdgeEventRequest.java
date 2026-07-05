package org.upc.edgeservice.edge.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record EdgeEventRequest(
        UUID clientEventId,
        String type,
        String aggregateType,
        String aggregateId,
        String payload,
        Instant occurredAt
) {
}
