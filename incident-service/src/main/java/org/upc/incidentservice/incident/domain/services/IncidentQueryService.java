package org.upc.incidentservice.incident.domain.services;

import org.upc.incidentservice.incident.domain.model.aggregates.Incident;
import org.upc.incidentservice.incident.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface IncidentQueryService {
    List<Incident> handle(GetAllIncidentsQuery query);

    Optional<Incident> handle(GetIncidentByIdQuery query);

    Optional<Incident> handle(GetIncidentByTechnicalIdQuery query);

    List<Incident> handle(GetIncidentsByStatusQuery query);

    List<Incident> handle(GetIncidentsByResponsibleUserIdQuery query);
}
