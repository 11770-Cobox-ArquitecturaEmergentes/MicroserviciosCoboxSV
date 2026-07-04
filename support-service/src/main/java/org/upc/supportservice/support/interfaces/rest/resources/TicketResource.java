package org.upc.supportservice.support.interfaces.rest.resources;

import org.upc.supportservice.support.domain.model.valueobjects.TicketCategory;
import org.upc.supportservice.support.domain.model.valueobjects.TicketPriority;
import org.upc.supportservice.support.domain.model.valueobjects.TicketStatus;

import java.util.Date;

public record TicketResource(
        Long id,
        String title,
        String description,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status,
        Long userId,
        Long assignedTo,
        Date createdAt,
        Date updatedAt
) {
}