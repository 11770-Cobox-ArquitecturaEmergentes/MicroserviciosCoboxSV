package org.upc.maintenanceservice.maintenance.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import org.upc.maintenanceservice.maintenance.domain.exceptions.InvalidMaintenanceScheduleStatusTransitionException;
import org.upc.maintenanceservice.maintenance.domain.exceptions.InvalidMaintenanceRulesException;
import org.upc.maintenanceservice.maintenance.domain.model.commands.*;
import org.upc.maintenanceservice.maintenance.domain.model.events.*;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceRule;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceScheduleStatus;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.VehicleId;
import org.upc.maintenanceservice.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class MaintenanceSchedule extends AuditableAbstractAggregateRoot<MaintenanceSchedule> {

    @Embedded
    private VehicleId vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceScheduleStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "maintenance_schedule_rules", joinColumns = @JoinColumn(name = "maintenance_schedule_id"))
    private List<MaintenanceRule> rules = new ArrayList<>();

    private LocalDateTime lastEvaluationAt;

    private LocalDateTime nextEvaluationAt;

    protected MaintenanceSchedule() {
    }

    public MaintenanceSchedule(CreateMaintenanceScheduleCommand command) {
        this.vehicleId = new VehicleId(command.vehicleId());
        this.rules = new ArrayList<>(command.rules());
        this.status = MaintenanceScheduleStatus.ACTIVE;
    }

    public void activate(ActivateMaintenanceScheduleCommand command) {
        if (this.status == MaintenanceScheduleStatus.ACTIVE) {
            return;
        }
        this.status = MaintenanceScheduleStatus.ACTIVE;
        this.registerEvent(new ScheduleActivatedEvent(this.getId()));
    }

    public void deactivate(DeactivateMaintenanceScheduleCommand command) {
        if (this.status == MaintenanceScheduleStatus.INACTIVE) {
            return;
        }
        this.status = MaintenanceScheduleStatus.INACTIVE;
        this.registerEvent(new ScheduleDeactivatedEvent(this.getId()));
    }

    public void evaluate(EvaluateMaintenanceScheduleCommand command) {
        if (this.status != MaintenanceScheduleStatus.ACTIVE) {
            throw new InvalidMaintenanceScheduleStatusTransitionException(this.status, MaintenanceScheduleStatus.ACTIVE);
        }
        if (this.rules.isEmpty()) {
            throw new InvalidMaintenanceRulesException();
        }
        this.lastEvaluationAt = LocalDateTime.now();
        this.nextEvaluationAt = this.lastEvaluationAt.plusDays(30);
        this.registerEvent(new ThresholdReachedEvent(this.getId(), this.vehicleId.vehicleId()));
    }

    public void updateRules(UpdateMaintenanceRulesCommand command) {
        if (command.rules() == null || command.rules().isEmpty()) {
            throw new InvalidMaintenanceRulesException();
        }
        this.rules = new ArrayList<>(command.rules());
        this.registerEvent(new RulesUpdatedEvent(this.getId()));
    }
}
