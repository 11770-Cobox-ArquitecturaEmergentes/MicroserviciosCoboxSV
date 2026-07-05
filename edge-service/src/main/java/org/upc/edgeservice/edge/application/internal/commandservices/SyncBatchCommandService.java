package org.upc.edgeservice.edge.application.internal.commandservices;

import org.upc.edgeservice.edge.interfaces.rest.resources.SyncBatchRequest;
import org.upc.edgeservice.edge.interfaces.rest.resources.SyncBatchResponse;

public interface SyncBatchCommandService {
    SyncBatchResponse handle(SyncBatchRequest request);
}
