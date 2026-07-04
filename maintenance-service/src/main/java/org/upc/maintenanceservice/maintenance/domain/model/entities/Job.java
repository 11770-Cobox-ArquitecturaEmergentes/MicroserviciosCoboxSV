package org.upc.maintenanceservice.maintenance.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import org.upc.maintenanceservice.maintenance.domain.model.commands.RegisterJobCommand;
import org.upc.maintenanceservice.maintenance.domain.model.aggregates.MaintenanceOrder;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.ChecklistItem;

@Entity
@Getter
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_order_id", nullable = false)
    private MaintenanceOrder maintenanceOrder;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean completed;

    protected Job() {
    }

    public Job(MaintenanceOrder maintenanceOrder, RegisterJobCommand command) {
        this.maintenanceOrder = maintenanceOrder;
        this.description = command.description();
        this.completed = command.completed() != null && command.completed();
    }

    public boolean isCompleted() {
        return completed;
    }

    public ChecklistItem toChecklistItem() {
        return new ChecklistItem(description, completed);
    }
}
