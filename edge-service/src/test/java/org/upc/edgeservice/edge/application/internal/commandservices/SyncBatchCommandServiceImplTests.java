package org.upc.edgeservice.edge.application.internal.commandservices;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.upc.edgeservice.edge.domain.model.valueobjects.SyncBatchStatus;
import org.upc.edgeservice.edge.domain.model.valueobjects.SyncItemStatus;
import org.upc.edgeservice.edge.infrastructure.persistence.jpa.repositories.EdgeEvidenceRepository;
import org.upc.edgeservice.edge.infrastructure.persistence.jpa.repositories.EdgeTelemetrySampleRepository;
import org.upc.edgeservice.edge.infrastructure.persistence.jpa.repositories.SyncBatchRepository;
import org.upc.edgeservice.edge.interfaces.rest.resources.EdgeEventRequest;
import org.upc.edgeservice.edge.interfaces.rest.resources.EdgeEvidenceRequest;
import org.upc.edgeservice.edge.interfaces.rest.resources.EdgeTelemetryRequest;
import org.upc.edgeservice.edge.interfaces.rest.resources.SyncBatchRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SyncBatchCommandServiceImplTests {

    @Autowired
    private SyncBatchCommandService syncBatchCommandService;

    @Autowired
    private SyncBatchRepository syncBatchRepository;

    @Autowired
    private EdgeEvidenceRepository evidenceRepository;

    @Autowired
    private EdgeTelemetrySampleRepository telemetryRepository;

    @Test
    void handleRecordsValidBatch() {
        var response = syncBatchCommandService.handle(validBatch(UUID.randomUUID()));

        assertThat(response.status()).isEqualTo(SyncBatchStatus.ACCEPTED);
        assertThat(response.batchId()).isNotNull();
        assertThat(response.results()).hasSize(3);
        assertThat(response.results()).allMatch(result -> result.status() == SyncItemStatus.RECORDED);
        assertThat(syncBatchRepository.count()).isEqualTo(1);
        assertThat(evidenceRepository.count()).isEqualTo(1);
        assertThat(telemetryRepository.count()).isEqualTo(1);
    }

    @Test
    void handleReturnsDuplicateForRepeatedBatchWithoutDuplicatingRows() {
        var clientBatchId = UUID.randomUUID();
        syncBatchCommandService.handle(validBatch(clientBatchId));

        var response = syncBatchCommandService.handle(validBatch(clientBatchId));

        assertThat(response.status()).isEqualTo(SyncBatchStatus.DUPLICATE);
        assertThat(response.results()).hasSize(3);
        assertThat(response.results()).allMatch(result -> result.status() == SyncItemStatus.DUPLICATE);
        assertThat(syncBatchRepository.count()).isEqualTo(1);
        assertThat(evidenceRepository.count()).isEqualTo(1);
        assertThat(telemetryRepository.count()).isEqualTo(1);
    }

    @Test
    void handlePartiallyAcceptsBatchWithInvalidTelemetry() {
        var invalidTelemetry = new EdgeTelemetryRequest(
                UUID.randomUUID(),
                20L,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(-77.0428),
                null,
                null,
                70,
                Instant.parse("2026-07-04T10:05:00Z")
        );
        var request = new SyncBatchRequest(
                UUID.randomUUID(),
                7L,
                "driver-phone-01",
                Instant.parse("2026-07-04T10:10:00Z"),
                List.of(validEvidence()),
                List.of(invalidTelemetry),
                List.of()
        );

        var response = syncBatchCommandService.handle(request);

        assertThat(response.status()).isEqualTo(SyncBatchStatus.PARTIALLY_ACCEPTED);
        assertThat(response.results()).hasSize(2);
        assertThat(response.results()).extracting("status")
                .containsExactly(SyncItemStatus.RECORDED, SyncItemStatus.REJECTED);
        assertThat(evidenceRepository.count()).isEqualTo(1);
        assertThat(telemetryRepository.count()).isZero();
    }

    private SyncBatchRequest validBatch(UUID clientBatchId) {
        return new SyncBatchRequest(
                clientBatchId,
                7L,
                "driver-phone-01",
                Instant.parse("2026-07-04T10:10:00Z"),
                List.of(validEvidence()),
                List.of(validTelemetry()),
                List.of(validEvent())
        );
    }

    private EdgeEvidenceRequest validEvidence() {
        return new EdgeEvidenceRequest(
                UUID.randomUUID(),
                100L,
                20L,
                "DELIVERY_PHOTO",
                "evidence/100/photo.jpg",
                "b6d81b360a5672d80c27430f39153e2c6f32f2255f6a071d9f8efb9bd2c7d1c2",
                "image/jpeg",
                2048L,
                Instant.parse("2026-07-04T10:00:00Z")
        );
    }

    private EdgeTelemetryRequest validTelemetry() {
        return new EdgeTelemetryRequest(
                UUID.randomUUID(),
                20L,
                BigDecimal.valueOf(-12.0464),
                BigDecimal.valueOf(-77.0428),
                BigDecimal.valueOf(8.5),
                BigDecimal.valueOf(32.1),
                70,
                Instant.parse("2026-07-04T10:05:00Z")
        );
    }

    private EdgeEventRequest validEvent() {
        return new EdgeEventRequest(
                UUID.randomUUID(),
                "ORDER_DELIVERED_OFFLINE",
                "ORDER",
                "100",
                "{\"note\":\"offline delivery\"}",
                Instant.parse("2026-07-04T10:06:00Z")
        );
    }
}
