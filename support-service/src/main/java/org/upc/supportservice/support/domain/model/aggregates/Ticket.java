package org.upc.supportservice.support.domain.model.aggregates;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.upc.supportservice.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import org.upc.supportservice.support.domain.model.valueobjects.TicketCategory;
import org.upc.supportservice.support.domain.model.valueobjects.TicketPriority;
import org.upc.supportservice.support.domain.model.valueobjects.TicketStatus;

@Entity
public class Ticket extends AuditableAbstractAggregateRoot<Ticket> {

    @Getter
    @NotBlank
    @Size(max = 120)
    private String title;

    @Getter
    @NotBlank
    @Size(max = 2000)
    private String description;

    @Getter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketCategory category;

    @Getter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @Getter
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Getter
    @NotNull
    @Column(nullable = false)
    private String userId;

    @Getter
    private Long assignedTo;

    public Ticket() {
        this.status = TicketStatus.OPEN;
    }

    public Ticket(String title, String description, TicketCategory category, TicketPriority priority, String userId, Long assignedTo) {
        this();
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.userId = userId;
        this.assignedTo = assignedTo;
    }

    public void updateInformation(String title, String description, TicketCategory category,
                                  TicketPriority priority, TicketStatus status, Long assignedTo) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = status;
        this.assignedTo = assignedTo;
    }
}