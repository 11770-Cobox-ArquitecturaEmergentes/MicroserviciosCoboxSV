package org.upc.maintenanceservice.maintenance.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import org.upc.maintenanceservice.maintenance.domain.exceptions.InvalidMaintenanceCurrencyException;
import org.upc.maintenanceservice.maintenance.domain.exceptions.InvalidMaintenanceOrderStatusTransitionException;
import org.upc.maintenanceservice.maintenance.domain.model.commands.*;
import org.upc.maintenanceservice.maintenance.domain.model.entities.Job;
import org.upc.maintenanceservice.maintenance.domain.model.entities.PartsRequest;
import org.upc.maintenanceservice.maintenance.domain.model.events.*;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.*;
import org.upc.maintenanceservice.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class MaintenanceOrder extends AuditableAbstractAggregateRoot<MaintenanceOrder> {

    @Embedded
    private VehicleId vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceTypes maintenanceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priorities priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceOrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Reason reason;

    @Column(name = "opening_odometer")
    private Long openingOdometer;

    @Column(name = "closing_odometer")
    private Long closingOdometer;

    @Embedded
    private Timelapse scheduledTimelapse;

    @Embedded
    private Money totalCost;

    private Long technicianId;

    @OneToMany(mappedBy = "maintenanceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Job> jobs = new ArrayList<>();

    @OneToMany(mappedBy = "maintenanceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartsRequest> partsRequests = new ArrayList<>();

    protected MaintenanceOrder() {
    }

    public MaintenanceOrder(CreateMaintenanceOrderCommand command) {
        this.vehicleId = new VehicleId(command.vehicleId());
        this.maintenanceType = command.maintenanceType();
        this.priority = command.priority();
        this.reason = command.reason();
        this.openingOdometer = command.openingOdometer();
        this.scheduledTimelapse = new Timelapse(command.scheduledTimelapseDays());
        this.technicianId = command.technicianId();
        this.status = MaintenanceOrderStatus.OPEN;
        this.registerEvent(new MaintenanceOrderCreatedEvent(this.getId(), command.vehicleId()));
    }

    public void schedule(ScheduleMaintenanceOrderCommand command) {
        this.ensureTransition(MaintenanceOrderStatus.SCHEDULED);
        this.scheduledTimelapse = new Timelapse(command.scheduledTimelapseDays());
        this.status = MaintenanceOrderStatus.SCHEDULED;
        this.registerEvent(new MaintenanceOrderScheduledEvent(this.getId(), command.scheduledTimelapseDays()));
    }

    public void start(StartMaintenanceOrderCommand command) {
        this.ensureTransition(MaintenanceOrderStatus.IN_PROGRESS);
        this.status = MaintenanceOrderStatus.IN_PROGRESS;
        this.registerEvent(new MaintenanceOrderStartedEvent(this.getId()));
    }

    public void complete(CompleteMaintenanceOrderCommand command) {
        this.ensureTransition(MaintenanceOrderStatus.COMPLETED);
        if (this.jobs.stream().anyMatch(job -> !job.isCompleted())) {
            throw new InvalidMaintenanceOrderStatusTransitionException(this.status, MaintenanceOrderStatus.COMPLETED);
        }
        if (this.partsRequests.stream().anyMatch(partsRequest -> !partsRequest.isReceived())) {
            throw new InvalidMaintenanceOrderStatusTransitionException(this.status, MaintenanceOrderStatus.COMPLETED);
        }
        if (this.openingOdometer != null && command.closingOdometer() < this.openingOdometer) {
            throw new InvalidMaintenanceOrderStatusTransitionException(this.status, MaintenanceOrderStatus.COMPLETED);
        }
        this.closingOdometer = command.closingOdometer();
        this.status = MaintenanceOrderStatus.COMPLETED;
        this.registerEvent(new MaintenanceOrderCompletedEvent(this.getId(), command.closingOdometer()));
    }

    public void cancel(CancelMaintenanceOrderCommand command) {
        if (this.status == MaintenanceOrderStatus.COMPLETED) {
            throw new InvalidMaintenanceOrderStatusTransitionException(this.status, MaintenanceOrderStatus.CANCELLED);
        }
        this.status = MaintenanceOrderStatus.CANCELLED;
        this.registerEvent(new MaintenanceOrderCancelledEvent(this.getId(), command.reason()));
    }

    public void registerJob(RegisterJobCommand command) {
        this.ensureNotTerminal();
        this.jobs.add(new Job(this, command));
        this.registerEvent(new PartsRequestedEvent(this.getId(), command.description(), 1));
    }

    public void requestParts(RequestPartsCommand command) {
        this.ensureNotTerminal();
        this.partsRequests.add(new PartsRequest(this, command));
        this.registerEvent(new PartsRequestedEvent(this.getId(), command.partName(), command.quantity()));
    }

    public void receiveParts(ReceivePartsCommand command) {
        var partsRequest = this.partsRequests.stream()
                .filter(request -> request.getId() != null && request.getId().equals(command.partsRequestId()))
                .findFirst()
                .orElseThrow(() -> new InvalidMaintenanceOrderStatusTransitionException(this.status, MaintenanceOrderStatus.IN_PROGRESS));
        partsRequest.markAsReceived();
        this.registerEvent(new PartsReceivedEvent(this.getId(), command.partsRequestId()));
    }

    public void registerCost(RegisterCostCommand command) {
        if (this.totalCost != null && this.totalCost.currency() != null && !this.totalCost.currency().equals(command.currency())) {
            throw new InvalidMaintenanceCurrencyException(this.totalCost.currency(), command.currency());
        }
        var currentAmount = this.totalCost == null || this.totalCost.amount() == null ? BigDecimal.ZERO : this.totalCost.amount();
        this.totalCost = new Money(currentAmount.add(command.amount()), command.currency());
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public List<PartsRequest> getPartsRequests() {
        return partsRequests;
    }

    private void ensureTransition(MaintenanceOrderStatus targetStatus) {
        if (!this.status.canTransitionTo(targetStatus)) {
            throw new InvalidMaintenanceOrderStatusTransitionException(this.status, targetStatus);
        }
    }

    private void ensureNotTerminal() {
        if (this.status == MaintenanceOrderStatus.COMPLETED || this.status == MaintenanceOrderStatus.CANCELLED) {
            throw new InvalidMaintenanceOrderStatusTransitionException(this.status, this.status);
        }
    }
}
