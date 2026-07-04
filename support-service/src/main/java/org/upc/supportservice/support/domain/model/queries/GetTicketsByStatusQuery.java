package org.upc.supportservice.support.domain.model.queries;

import org.upc.supportservice.support.domain.model.valueobjects.TicketStatus;

public record GetTicketsByStatusQuery(TicketStatus status) {
}