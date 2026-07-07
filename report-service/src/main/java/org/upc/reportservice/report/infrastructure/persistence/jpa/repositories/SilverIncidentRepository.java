package org.upc.reportservice.report.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upc.reportservice.report.domain.model.entities.SilverIncident;
import org.springframework.stereotype.Repository;

@Repository
public interface SilverIncidentRepository extends JpaRepository<SilverIncident, Long> {
}
