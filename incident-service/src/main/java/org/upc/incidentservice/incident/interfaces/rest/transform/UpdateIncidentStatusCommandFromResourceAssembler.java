package org.upc.incidentservice.incident.interfaces.rest.transform;

import org.upc.incidentservice.incident.domain.model.commands.UpdateIncidentStatusCommand;
import org.upc.incidentservice.incident.interfaces.rest.resources.UpdateIncidentStatusResource;

import java.util.UUID;

public class UpdateIncidentStatusCommandFromResourceAssembler {

    public static UpdateIncidentStatusCommand toCommandFromResource(UUID incidentId, UpdateIncidentStatusResource resource) {
        return new UpdateIncidentStatusCommand(incidentId, resource.status());
    }
}
