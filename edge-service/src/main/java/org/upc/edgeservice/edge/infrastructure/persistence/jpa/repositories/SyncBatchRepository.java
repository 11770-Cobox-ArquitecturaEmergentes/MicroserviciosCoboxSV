package org.upc.edgeservice.edge.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upc.edgeservice.edge.domain.model.aggregates.SyncBatch;

import java.util.Optional;
import java.util.UUID;

public interface SyncBatchRepository extends JpaRepository<SyncBatch, Long> {
    Optional<SyncBatch> findByClientBatchId(UUID clientBatchId);
}
