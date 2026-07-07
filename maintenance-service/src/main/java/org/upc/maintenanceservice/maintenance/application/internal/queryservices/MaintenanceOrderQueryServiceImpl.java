package org.upc.maintenanceservice.maintenance.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.upc.maintenanceservice.maintenance.domain.model.aggregates.MaintenanceOrder;
import org.upc.maintenanceservice.maintenance.domain.model.queries.*;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceOrderStatus;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.VehicleId;
import org.upc.maintenanceservice.maintenance.domain.services.MaintenanceOrderQueryService;
import org.upc.maintenanceservice.maintenance.infrastructure.persistence.jpa.repositories.MaintenanceOrderRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceOrderQueryServiceImpl implements MaintenanceOrderQueryService {

    private static final List<MaintenanceOrderStatus> OPEN_STATUSES = List.of(
            MaintenanceOrderStatus.OPEN,
            MaintenanceOrderStatus.SCHEDULED,
            MaintenanceOrderStatus.IN_PROGRESS
    );

    private final MaintenanceOrderRepository maintenanceOrderRepository;

    public MaintenanceOrderQueryServiceImpl(MaintenanceOrderRepository maintenanceOrderRepository) {
        this.maintenanceOrderRepository = maintenanceOrderRepository;
    }

    @Override
    public Optional<MaintenanceOrder> handle(GetMaintenanceOrderByIdQuery query) {
        return maintenanceOrderRepository.findById(query.orderId());
    }

    @Override
    public List<MaintenanceOrder> handle(GetMaintenanceOrderHistoryQuery query) {
        return maintenanceOrderRepository.findByVehicleIdOrderByCreatedAtDesc(new VehicleId(query.vehicleId()));
    }

    @Override
    public List<MaintenanceOrder> handle(GetOpenMaintenanceOrdersByVehicleIdQuery query) {
        return maintenanceOrderRepository.findByVehicleIdAndStatusIn(new VehicleId(query.vehicleId()), OPEN_STATUSES);
    }

    @Override
    public List<MaintenanceOrder> handle(GetMaintenanceOrdersByStatusQuery query) {
        return maintenanceOrderRepository.findByStatus(query.status());
    }

    @Override
    public boolean handle(HasMaintenanceOpenOrderForVehicleIdQuery query) {
        return maintenanceOrderRepository.existsByVehicleIdAndStatusIn(new VehicleId(query.vehicleId()), OPEN_STATUSES);
    }

    @Override
    public List<MaintenanceOrder> handle(GetAllMaintenanceOrdersQuery query) {
        return maintenanceOrderRepository.findAll();
    }
}
