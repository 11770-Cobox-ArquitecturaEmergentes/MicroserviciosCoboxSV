package org.upc.incidentservice.incident.interfaces.rest.transform;

import org.upc.incidentservice.incident.domain.model.commands.AssignResponsibleUserCommand;
import org.upc.incidentservice.incident.interfaces.rest.resources.AssignResponsibleUserResource;

import java.util.UUID;

public class AssignResponsibleUserCommandFromResourceAssembler {

    public static AssignResponsibleUserCommand toCommandFromResource(UUID incidentId, AssignResponsibleUserResource resource) {
        return new AssignResponsibleUserCommand(incidentId, resource.responsibleUserId());
    }
}
