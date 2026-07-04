package org.upc.supportservice.support.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.upc.supportservice.support.domain.model.aggregates.Ticket;
import org.upc.supportservice.support.domain.model.valueobjects.TicketCategory;
import org.upc.supportservice.support.domain.model.valueobjects.TicketPriority;
import org.upc.supportservice.support.domain.model.valueobjects.TicketStatus;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByUserId(Long userId);
    List<Ticket> findAllByStatus(TicketStatus status);
    List<Ticket> findAllByPriority(TicketPriority priority);
    List<Ticket> findAllByCategory(TicketCategory category);
}