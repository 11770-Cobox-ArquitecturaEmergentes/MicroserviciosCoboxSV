package org.upc.incidentservice.incident.domain.services;

import org.upc.incidentservice.incident.domain.model.commands.AssignResponsibleUserCommand;
import org.upc.incidentservice.incident.domain.model.commands.CreateIncidentCommand;
import org.upc.incidentservice.incident.domain.model.commands.UpdateIncidentStatusCommand;

public interface IncidentCommandService {
    Long handle(CreateIncidentCommand command);

    void handle(UpdateIncidentStatusCommand command);

    void handle(AssignResponsibleUserCommand command);
}
