package org.upc.supportservice.support.domain.services;

import org.upc.supportservice.support.domain.model.aggregates.Ticket;
import org.upc.supportservice.support.domain.model.queries.GetAllTicketsQuery;
import org.upc.supportservice.support.domain.model.queries.GetTicketByIdQuery;
import org.upc.supportservice.support.domain.model.queries.GetTicketsByCategoryQuery;
import org.upc.supportservice.support.domain.model.queries.GetTicketsByPriorityQuery;
import org.upc.supportservice.support.domain.model.queries.GetTicketsByStatusQuery;
import org.upc.supportservice.support.domain.model.queries.GetTicketsByUserIdQuery;

import java.util.List;
import java.util.Optional;

public interface TicketQueryService {
    List<Ticket> handle(GetAllTicketsQuery query);
    Optional<Ticket> handle(GetTicketByIdQuery query);
    List<Ticket> handle(GetTicketsByUserIdQuery query);
    List<Ticket> handle(GetTicketsByStatusQuery query);
    List<Ticket> handle(GetTicketsByPriorityQuery query);
    List<Ticket> handle(GetTicketsByCategoryQuery query);
}