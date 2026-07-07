package org.upc.aivalidationservice.validation.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upc.aivalidationservice.validation.domain.model.entities.AiAlert;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.AlertStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiAlertRepository extends JpaRepository<AiAlert, Long> {
    Optional<AiAlert> findByAlertId(UUID alertId);
    List<AiAlert> findByClientEvidenceId(UUID clientEvidenceId);
    List<AiAlert> findByStatusOrderByCreatedAtDesc(AlertStatus status);
    Optional<AiAlert> findByClientEvidenceIdAndType(UUID clientEvidenceId, String type);
}
