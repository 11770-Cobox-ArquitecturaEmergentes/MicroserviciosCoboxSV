package org.upc.incidentservice.incident.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.upc.incidentservice.incident.domain.exceptions.IncidentNotFoundException;
import org.upc.incidentservice.incident.domain.model.aggregates.Incident;
import org.upc.incidentservice.incident.domain.model.commands.AssignResponsibleUserCommand;
import org.upc.incidentservice.incident.domain.model.commands.CreateIncidentCommand;
import org.upc.incidentservice.incident.domain.model.commands.UpdateIncidentStatusCommand;
import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentId;
import org.upc.incidentservice.incident.domain.services.IncidentCommandService;
import org.upc.incidentservice.incident.infrastructure.persistence.jpa.repositories.IncidentRepository;

import java.util.UUID;

@Service
public class IncidentCommandServiceImpl implements IncidentCommandService {

    private final IncidentRepository incidentRepository;

    public IncidentCommandServiceImpl(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Override
    public Long handle(CreateIncidentCommand command) {
        var incident = new Incident(command);
        incidentRepository.save(incident);
        return incident.getId();
    }

    @Override
    public void handle(UpdateIncidentStatusCommand command) {
        var incidentId = new IncidentId(command.incidentId());
        incidentRepository.findByIncidentId(incidentId)
                .map(incident -> {
                    incident.updateStatus(command);
                    incidentRepository.save(incident);
                    return incident.getId();
                })
                .orElseThrow(() -> new IncidentNotFoundException(command.incidentId()));
    }

    @Override
    public void handle(AssignResponsibleUserCommand command) {
        var incidentId = new IncidentId(command.incidentId());
        incidentRepository.findByIncidentId(incidentId)
                .map(incident -> {
                    incident.assignResponsible(command);
                    incidentRepository.save(incident);
                    return incident.getId();
                })
                .orElseThrow(() -> new IncidentNotFoundException(command.incidentId()));
    }
}
