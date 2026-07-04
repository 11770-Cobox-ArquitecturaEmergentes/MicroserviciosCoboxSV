package org.upc.supportservice.support.domain.model.queries;

import org.upc.supportservice.support.domain.model.valueobjects.TicketPriority;

public record GetTicketsByPriorityQuery(TicketPriority priority) {
}