package org.upc.reportservice.report.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upc.reportservice.report.domain.model.entities.BronzeEvent;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BronzeEventRepository extends JpaRepository<BronzeEvent, Long> {
    List<BronzeEvent> findByProcessedFalse();
}
