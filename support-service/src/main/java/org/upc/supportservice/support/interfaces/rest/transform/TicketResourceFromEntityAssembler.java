package org.upc.supportservice.support.interfaces.rest.transform;

import org.upc.supportservice.support.domain.model.aggregates.Ticket;
import org.upc.supportservice.support.interfaces.rest.resources.TicketResource;

public class TicketResourceFromEntityAssembler {
    public static TicketResource toResourceFromEntity(Ticket entity) {
        return new TicketResource(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getPriority(),
                entity.getStatus(),
                entity.getUserId(),
                entity.getAssignedTo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}