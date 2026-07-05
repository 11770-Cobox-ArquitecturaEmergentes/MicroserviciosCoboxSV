package org.upc.aivalidationservice.validation.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upc.aivalidationservice.validation.domain.model.aggregates.EvidenceAnalysis;

import java.util.Optional;
import java.util.UUID;

public interface EvidenceAnalysisRepository extends JpaRepository<EvidenceAnalysis, Long> {
    Optional<EvidenceAnalysis> findByClientEvidenceId(UUID clientEvidenceId);
    boolean existsByClientEvidenceId(UUID clientEvidenceId);
}
