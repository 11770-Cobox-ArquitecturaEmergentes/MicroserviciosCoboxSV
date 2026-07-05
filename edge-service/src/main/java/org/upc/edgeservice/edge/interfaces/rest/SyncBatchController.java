package org.upc.edgeservice.edge.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.upc.edgeservice.edge.application.internal.commandservices.SyncBatchCommandService;
import org.upc.edgeservice.edge.domain.model.valueobjects.SyncBatchStatus;
import org.upc.edgeservice.edge.interfaces.rest.resources.SyncBatchRequest;
import org.upc.edgeservice.edge.interfaces.rest.resources.SyncBatchResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/edge/sync-batches", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Edge Sync", description = "Offline mobile synchronization endpoints")
public class SyncBatchController {

    private final SyncBatchCommandService syncBatchCommandService;

    public SyncBatchController(SyncBatchCommandService syncBatchCommandService) {
        this.syncBatchCommandService = syncBatchCommandService;
    }

    @Operation(summary = "Synchronize an offline mobile batch",
            description = "Records offline evidence metadata, GPS telemetry and domain events using client-generated UUIDs for idempotency.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Batch processed",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SyncBatchResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Batch rejected",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SyncBatchResponse.class)))
            })
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<SyncBatchResponse> synchronize(@RequestBody(required = false) SyncBatchRequest request) {
        var response = syncBatchCommandService.handle(request);
        if (response.status() == SyncBatchStatus.REJECTED && response.batchId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
