package org.upc.maintenanceservice.maintenance.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.upc.maintenanceservice.maintenance.domain.exceptions.DuplicateOpenMaintenanceOrderException;
import org.upc.maintenanceservice.maintenance.domain.exceptions.MaintenanceOrderNotFoundException;
import org.upc.maintenanceservice.maintenance.domain.model.aggregates.MaintenanceOrder;
import org.upc.maintenanceservice.maintenance.domain.model.commands.*;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceOrderStatus;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.VehicleId;
import org.upc.maintenanceservice.maintenance.domain.services.MaintenanceOrderCommandService;
import org.upc.maintenanceservice.maintenance.infrastructure.messaging.MaintenanceReportEventPublisher;
import org.upc.maintenanceservice.maintenance.infrastructure.persistence.jpa.repositories.MaintenanceOrderRepository;

import java.util.List;

@Service
public class MaintenanceOrderCommandServiceImpl implements MaintenanceOrderCommandService {

    private static final List<MaintenanceOrderStatus> OPEN_STATUSES = List.of(
            MaintenanceOrderStatus.OPEN,
            MaintenanceOrderStatus.SCHEDULED,
            MaintenanceOrderStatus.IN_PROGRESS
    );

    private final MaintenanceOrderRepository maintenanceOrderRepository;
    private final MaintenanceReportEventPublisher reportEventPublisher;

    public MaintenanceOrderCommandServiceImpl(MaintenanceOrderRepository maintenanceOrderRepository,
                                              MaintenanceReportEventPublisher reportEventPublisher) {
        this.maintenanceOrderRepository = maintenanceOrderRepository;
        this.reportEventPublisher = reportEventPublisher;
    }

    @Override
    public Long handle(CreateMaintenanceOrderCommand command) {
        var vehicleId = new VehicleId(command.vehicleId());
        if (maintenanceOrderRepository.existsByVehicleIdAndStatusIn(vehicleId, OPEN_STATUSES)) {
            throw new DuplicateOpenMaintenanceOrderException(command.vehicleId());
        }
        var maintenanceOrder = new MaintenanceOrder(command);
        maintenanceOrderRepository.save(maintenanceOrder);
        reportEventPublisher.publishOrder("maintenance.order-created", maintenanceOrder);
        return maintenanceOrder.getId();
    }

    @Override
    public void handle(ScheduleMaintenanceOrderCommand command) {
        maintenanceOrderRepository.findById(command.orderId()).map(order -> {
            order.schedule(command);
            maintenanceOrderRepository.save(order);
            reportEventPublisher.publishOrder("maintenance.order-scheduled", order);
            return order.getId();
        }).orElseThrow(() -> new MaintenanceOrderNotFoundException(command.orderId()));
    }

    @Override
    public void handle(StartMaintenanceOrderCommand command) {
        maintenanceOrderRepository.findById(command.orderId()).map(order -> {
            order.start(command);
            maintenanceOrderRepository.save(order);
            reportEventPublisher.publishOrder("maintenance.order-started", order);
            return order.getId();
        }).orElseThrow(() -> new MaintenanceOrderNotFoundException(command.orderId()));
    }

    @Override
    public void handle(CompleteMaintenanceOrderCommand command) {
        maintenanceOrderRepository.findById(command.orderId()).map(order -> {
            order.complete(command);
            maintenanceOrderRepository.save(order);
            reportEventPublisher.publishOrder("maintenance.order-completed", order);
            return order.getId();
        }).orElseThrow(() -> new MaintenanceOrderNotFoundException(command.orderId()));
    }

    @Override
    public void handle(CancelMaintenanceOrderCommand command) {
        maintenanceOrderRepository.findById(command.orderId()).map(order -> {
            order.cancel(command);
            maintenanceOrderRepository.save(order);
            reportEventPublisher.publishOrder("maintenance.order-cancelled", order);
            return order.getId();
        }).orElseThrow(() -> new MaintenanceOrderNotFoundException(command.orderId()));
    }

    @Override
    public void handle(RegisterJobCommand command) {
        maintenanceOrderRepository.findById(command.orderId()).map(order -> {
            order.registerJob(command);
            maintenanceOrderRepository.save(order);
            reportEventPublisher.publishOrder("maintenance.job-registered", order);
            return order.getId();
        }).orElseThrow(() -> new MaintenanceOrderNotFoundException(command.orderId()));
    }

    @Override
    public void handle(RequestPartsCommand command) {
        maintenanceOrderRepository.findById(command.orderId()).map(order -> {
            order.requestParts(command);
            maintenanceOrderRepository.save(order);
            reportEventPublisher.publishOrder("maintenance.parts-requested", order);
            return order.getId();
        }).orElseThrow(() -> new MaintenanceOrderNotFoundException(command.orderId()));
    }

    @Override
    public void handle(ReceivePartsCommand command) {
        maintenanceOrderRepository.findById(command.orderId()).map(order -> {
            order.receiveParts(command);
            maintenanceOrderRepository.save(order);
            reportEventPublisher.publishOrder("maintenance.parts-received", order);
            return order.getId();
        }).orElseThrow(() -> new MaintenanceOrderNotFoundException(command.orderId()));
    }

    @Override
    public void handle(RegisterCostCommand command) {
        maintenanceOrderRepository.findById(command.orderId()).map(order -> {
            order.registerCost(command);
            maintenanceOrderRepository.save(order);
            reportEventPublisher.publishOrder("maintenance.cost-registered", order);
            return order.getId();
        }).orElseThrow(() -> new MaintenanceOrderNotFoundException(command.orderId()));
    }
}
