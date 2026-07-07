package org.upc.edgeservice.edge.infrastructure.persistence.jpa.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.upc.edgeservice.edge.domain.model.aggregates.SyncBatch;
import org.upc.edgeservice.edge.domain.model.entities.EdgeEventRecord;
import org.upc.edgeservice.edge.domain.model.entities.EdgeEvidence;
import org.upc.edgeservice.edge.domain.model.entities.EdgeTelemetrySample;
import org.upc.edgeservice.edge.domain.model.valueobjects.SyncBatchStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class EdgeRepositoryConstraintsTests {

    @Autowired
    private SyncBatchRepository syncBatchRepository;

    @Autowired
    private EdgeEvidenceRepository evidenceRepository;

    @Autowired
    private EdgeTelemetrySampleRepository telemetryRepository;

    @Autowired
    private EdgeEventRecordRepository eventRepository;

    @Test
    void syncBatchClientBatchIdIsUnique() {
        var clientBatchId = UUID.randomUUID();
        syncBatchRepository.saveAndFlush(batch(clientBatchId));

        assertThatThrownBy(() -> syncBatchRepository.saveAndFlush(batch(clientBatchId)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void evidenceClientEvidenceIdIsUnique() {
        var batch = syncBatchRepository.saveAndFlush(batch(UUID.randomUUID()));
        var clientEvidenceId = UUID.randomUUID();
        evidenceRepository.saveAndFlush(evidence(clientEvidenceId, batch));

        assertThatThrownBy(() -> evidenceRepository.saveAndFlush(evidence(clientEvidenceId, batch)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void telemetryClientTelemetryIdIsUnique() {
        var batch = syncBatchRepository.saveAndFlush(batch(UUID.randomUUID()));
        var clientTelemetryId = UUID.randomUUID();
        telemetryRepository.saveAndFlush(telemetry(clientTelemetryId, batch));

        assertThatThrownBy(() -> telemetryRepository.saveAndFlush(telemetry(clientTelemetryId, batch)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void eventClientEventIdIsUnique() {
        var batch = syncBatchRepository.saveAndFlush(batch(UUID.randomUUID()));
        var clientEventId = UUID.randomUUID();
        eventRepository.saveAndFlush(event(clientEventId, batch));

        assertThatThrownBy(() -> eventRepository.saveAndFlush(event(clientEventId, batch)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private SyncBatch batch(UUID clientBatchId) {
        return new SyncBatch(
                clientBatchId,
                7L,
                "driver-phone-01",
                Instant.parse("2026-07-04T10:10:00Z"),
                SyncBatchStatus.ACCEPTED
        );
    }

    private EdgeEvidence evidence(UUID clientEvidenceId, SyncBatch batch) {
        return new EdgeEvidence(
                clientEvidenceId,
                batch,
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

    private EdgeTelemetrySample telemetry(UUID clientTelemetryId, SyncBatch batch) {
        return new EdgeTelemetrySample(
                clientTelemetryId,
                batch,
                20L,
                BigDecimal.valueOf(-12.0464),
                BigDecimal.valueOf(-77.0428),
                BigDecimal.valueOf(8.5),
                BigDecimal.valueOf(32.1),
                70,
                Instant.parse("2026-07-04T10:05:00Z")
        );
    }

    private EdgeEventRecord event(UUID clientEventId, SyncBatch batch) {
        return new EdgeEventRecord(
                clientEventId,
                batch,
                "ORDER_DELIVERED_OFFLINE",
                "ORDER",
                "100",
                "{\"note\":\"offline delivery\"}",
                Instant.parse("2026-07-04T10:06:00Z")
        );
    }
}
