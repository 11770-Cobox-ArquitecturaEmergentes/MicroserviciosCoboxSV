package org.upc.edgeservice.edge.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upc.edgeservice.edge.domain.model.entities.EdgeTelemetrySample;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EdgeTelemetrySampleRepository extends JpaRepository<EdgeTelemetrySample, Long> {
    Optional<EdgeTelemetrySample> findByClientTelemetryId(UUID clientTelemetryId);
    List<EdgeTelemetrySample> findBySyncBatchId(Long syncBatchId);
}
