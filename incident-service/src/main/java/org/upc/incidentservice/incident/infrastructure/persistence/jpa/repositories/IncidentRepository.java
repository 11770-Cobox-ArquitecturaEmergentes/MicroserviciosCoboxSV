package org.upc.incidentservice.incident.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.upc.incidentservice.incident.domain.model.aggregates.Incident;
import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentId;
import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentStatus;
import org.upc.incidentservice.incident.domain.model.valueobjects.ResponsibleUserId;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findByIncidentId(IncidentId incidentId);

    List<Incident> findByStatus(IncidentStatus status);

    List<Incident> findByResponsibleUserId(ResponsibleUserId responsibleUserId);
}
