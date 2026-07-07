package org.upc.supportservice.support.interfaces.rest.transform;

import org.upc.supportservice.support.domain.model.commands.CreateTicketCommand;
import org.upc.supportservice.support.interfaces.rest.resources.CreateTicketResource;

public class CreateTicketCommandFromResourceAssembler {
    public static CreateTicketCommand toCommandFromResource(CreateTicketResource resource, String userId) {
        return new CreateTicketCommand(
                resource.title(),
                resource.description(),
                resource.category(),
                resource.priority(),
                userId,
                resource.assignedTo()
        );
    }
}