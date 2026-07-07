package org.upc.edgeservice.edge.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.upc.edgeservice.edge.domain.model.entities.EdgeEvidence;
import org.upc.edgeservice.edge.domain.model.entities.EdgeTelemetrySample;
import org.upc.edgeservice.edge.infrastructure.persistence.jpa.repositories.EdgeEvidenceRepository;
import org.upc.edgeservice.edge.infrastructure.persistence.jpa.repositories.EdgeTelemetrySampleRepository;
import org.upc.edgeservice.edge.interfaces.rest.resources.EdgeEvidenceResource;
import org.upc.edgeservice.edge.interfaces.rest.resources.EdgeTelemetryResource;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/edge")
public class EdgeQueryController {

    private final EdgeEvidenceRepository edgeEvidenceRepository;
    private final EdgeTelemetrySampleRepository edgeTelemetrySampleRepository;

    public EdgeQueryController(EdgeEvidenceRepository edgeEvidenceRepository,
                               EdgeTelemetrySampleRepository edgeTelemetrySampleRepository) {
        this.edgeEvidenceRepository = edgeEvidenceRepository;
        this.edgeTelemetrySampleRepository = edgeTelemetrySampleRepository;
    }

    @GetMapping("/evidences/{clientEvidenceId}")
    public ResponseEntity<EdgeEvidenceResource> getEvidence(@PathVariable UUID clientEvidenceId) {
        return edgeEvidenceRepository.findByClientEvidenceId(clientEvidenceId)
                .map(this::toResource)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/routes/{routeId}/telemetry")
    public ResponseEntity<List<EdgeTelemetryResource>> getTelemetryByRoute(@PathVariable Long routeId) {
        var samples = edgeTelemetrySampleRepository.findByRouteIdOrderByCapturedAtAsc(routeId)
                .stream()
                .map(this::toResource)
                .toList();
        return ResponseEntity.ok(samples);
    }

    private EdgeEvidenceResource toResource(EdgeEvidence evidence) {
        return new EdgeEvidenceResource(
                evidence.getClientEvidenceId(),
                evidence.getOrderId(),
                evidence.getRouteId(),
                evidence.getType(),
                evidence.getObjectKey(),
                evidence.getSha256(),
                evidence.getMimeType(),
                evidence.getSizeBytes(),
                evidence.getCapturedAt(),
                evidence.getStatus()
        );
    }

    private EdgeTelemetryResource toResource(EdgeTelemetrySample telemetry) {
        return new EdgeTelemetryResource(
                telemetry.getClientTelemetryId(),
                telemetry.getRouteId(),
                telemetry.getLatitude(),
                telemetry.getLongitude(),
                telemetry.getAccuracyMeters(),
                telemetry.getSpeedKmh(),
                telemetry.getBatteryLevel(),
                telemetry.getCapturedAt()
        );
    }
}
