package org.upc.edgeservice.edge.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upc.edgeservice.edge.domain.model.entities.EdgeEvidence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EdgeEvidenceRepository extends JpaRepository<EdgeEvidence, Long> {
    Optional<EdgeEvidence> findByClientEvidenceId(UUID clientEvidenceId);
    List<EdgeEvidence> findBySyncBatchId(Long syncBatchId);
}
