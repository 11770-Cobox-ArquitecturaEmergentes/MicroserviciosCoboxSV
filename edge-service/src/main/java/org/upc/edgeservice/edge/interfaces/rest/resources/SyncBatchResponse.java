package org.upc.edgeservice.edge.interfaces.rest.resources;

import org.upc.edgeservice.edge.domain.model.valueobjects.SyncBatchStatus;

import java.util.List;
import java.util.UUID;

public record SyncBatchResponse(
        Long batchId,
        UUID clientBatchId,
        SyncBatchStatus status,
        List<SyncItemResultResource> results
) {
}
