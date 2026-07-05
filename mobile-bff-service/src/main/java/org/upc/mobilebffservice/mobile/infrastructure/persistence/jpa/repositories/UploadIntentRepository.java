package org.upc.mobilebffservice.mobile.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upc.mobilebffservice.mobile.domain.model.aggregates.UploadIntent;

import java.util.Optional;
import java.util.UUID;

public interface UploadIntentRepository extends JpaRepository<UploadIntent, Long> {
    Optional<UploadIntent> findByUploadIntentId(UUID uploadIntentId);
    Optional<UploadIntent> findByClientEvidenceId(UUID clientEvidenceId);
}
