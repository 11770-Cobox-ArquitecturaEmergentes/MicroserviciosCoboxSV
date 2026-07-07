package org.upc.edgeservice.edge.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upc.edgeservice.edge.domain.model.entities.EdgeEventRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EdgeEventRecordRepository extends JpaRepository<EdgeEventRecord, Long> {
    Optional<EdgeEventRecord> findByClientEventId(UUID clientEventId);
    List<EdgeEventRecord> findBySyncBatchId(Long syncBatchId);
}
