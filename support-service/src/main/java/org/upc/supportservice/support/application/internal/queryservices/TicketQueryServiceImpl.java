package org.upc.supportservice.support.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.upc.supportservice.support.domain.model.aggregates.Ticket;
import org.upc.supportservice.support.domain.model.queries.GetAllTicketsQuery;
import org.upc.supportservice.support.domain.model.queries.GetTicketByIdQuery;
import org.upc.supportservice.support.domain.model.queries.GetTicketsByCategoryQuery;
import org.upc.supportservice.support.domain.model.queries.GetTicketsByPriorityQuery;
import org.upc.supportservice.support.domain.model.queries.GetTicketsByStatusQuery;
import org.upc.supportservice.support.domain.model.queries.GetTicketsByUserIdQuery;
import org.upc.supportservice.support.domain.services.TicketQueryService;
import org.upc.supportservice.support.infrastructure.persistence.jpa.repositories.TicketRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TicketQueryServiceImpl implements TicketQueryService {

    private final TicketRepository ticketRepository;

    public TicketQueryServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public List<Ticket> handle(GetAllTicketsQuery query) {
        return ticketRepository.findAll();
    }

    @Override
    public Optional<Ticket> handle(GetTicketByIdQuery query) {
        return ticketRepository.findById(query.ticketId());
    }

    @Override
    public List<Ticket> handle(GetTicketsByUserIdQuery query) {
        return ticketRepository.findAllByUserId(query.userId());
    }

    @Override
    public List<Ticket> handle(GetTicketsByStatusQuery query) {
        return ticketRepository.findAllByStatus(query.status());
    }

    @Override
    public List<Ticket> handle(GetTicketsByPriorityQuery query) {
        return ticketRepository.findAllByPriority(query.priority());
    }

    @Override
    public List<Ticket> handle(GetTicketsByCategoryQuery query) {
        return ticketRepository.findAllByCategory(query.category());
    }
}