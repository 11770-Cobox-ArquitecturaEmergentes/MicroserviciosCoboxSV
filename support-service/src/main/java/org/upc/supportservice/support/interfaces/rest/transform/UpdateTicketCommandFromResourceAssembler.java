package org.upc.supportservice.support.interfaces.rest.transform;

import org.upc.supportservice.support.domain.model.commands.UpdateTicketCommand;
import org.upc.supportservice.support.interfaces.rest.resources.UpdateTicketResource;

public class UpdateTicketCommandFromResourceAssembler {
    public static UpdateTicketCommand toCommandFromResource(UpdateTicketResource resource, Long ticketId) {
        return new UpdateTicketCommand(
                ticketId,
                resource.title(),
                resource.description(),
                resource.category(),
                resource.priority(),
                resource.status(),
                resource.assignedTo()
        );
    }
}