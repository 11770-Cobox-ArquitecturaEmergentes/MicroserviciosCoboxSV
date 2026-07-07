package org.upc.incidentservice.incident.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.upc.incidentservice.incident.domain.model.aggregates.Incident;
import org.upc.incidentservice.incident.domain.model.queries.*;
import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentId;
import org.upc.incidentservice.incident.domain.model.valueobjects.ResponsibleUserId;
import org.upc.incidentservice.incident.domain.services.IncidentQueryService;
import org.upc.incidentservice.incident.infrastructure.persistence.jpa.repositories.IncidentRepository;

import java.util.List;
import java.util.Optional;

@Service
public class IncidentQueryServiceImpl implements IncidentQueryService {

    private final IncidentRepository incidentRepository;

    public IncidentQueryServiceImpl(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Override
    public List<Incident> handle(GetAllIncidentsQuery query) {
        return incidentRepository.findAll();
    }

    @Override
    public Optional<Incident> handle(GetIncidentByIdQuery query) {
        return incidentRepository.findByIncidentId(new IncidentId(query.incidentId()));
    }

    @Override
    public Optional<Incident> handle(GetIncidentByTechnicalIdQuery query) {
        return incidentRepository.findById(query.incidentId());
    }

    @Override
    public List<Incident> handle(GetIncidentsByStatusQuery query) {
        return incidentRepository.findByStatus(query.status());
    }

    @Override
    public List<Incident> handle(GetIncidentsByResponsibleUserIdQuery query) {
        return incidentRepository.findByResponsibleUserId(new ResponsibleUserId(query.responsibleUserId()));
    }

    @Override
    public Optional<Incident> handle(GetIncidentBySourceAlertIdQuery query) {
        return incidentRepository.findBySourceAlertId(query.sourceAlertId());
    }
}
