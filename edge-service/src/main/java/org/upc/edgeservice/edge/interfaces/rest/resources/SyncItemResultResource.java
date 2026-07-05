package org.upc.edgeservice.edge.interfaces.rest.resources;

import org.upc.edgeservice.edge.domain.model.valueobjects.SyncItemStatus;

import java.util.UUID;

public record SyncItemResultResource(
        UUID clientId,
        Long serverId,
        SyncItemStatus status,
        String message
) {
}
