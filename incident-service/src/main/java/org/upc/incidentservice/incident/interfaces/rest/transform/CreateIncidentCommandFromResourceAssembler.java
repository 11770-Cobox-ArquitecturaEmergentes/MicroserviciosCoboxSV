package org.upc.incidentservice.incident.interfaces.rest.transform;

import org.upc.incidentservice.incident.domain.model.commands.CreateIncidentCommand;
import org.upc.incidentservice.incident.interfaces.rest.resources.CreateIncidentResource;

public class CreateIncidentCommandFromResourceAssembler {

    public static CreateIncidentCommand toCommandFromResource(CreateIncidentResource resource) {
        return new CreateIncidentCommand(
                resource.type(),
                resource.description(),
                resource.severity(),
                resource.responsibleUserId(),
                resource.sourceType(),
                resource.sourceAlertId(),
                resource.sourceClientEvidenceId()
        );
    }
}
