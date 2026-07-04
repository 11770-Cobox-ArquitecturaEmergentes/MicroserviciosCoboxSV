package org.upc.maintenanceservice.maintenance.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.upc.maintenanceservice.maintenance.domain.model.aggregates.MaintenanceSchedule;
import org.upc.maintenanceservice.maintenance.domain.model.queries.*;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceScheduleStatus;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.VehicleId;
import org.upc.maintenanceservice.maintenance.domain.services.MaintenanceScheduleQueryService;
import org.upc.maintenanceservice.maintenance.infrastructure.persistence.jpa.repositories.MaintenanceScheduleRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceScheduleQueryServiceImpl implements MaintenanceScheduleQueryService {

    private final MaintenanceScheduleRepository maintenanceScheduleRepository;

    public MaintenanceScheduleQueryServiceImpl(MaintenanceScheduleRepository maintenanceScheduleRepository) {
        this.maintenanceScheduleRepository = maintenanceScheduleRepository;
    }

    @Override
    public Optional<MaintenanceSchedule> handle(GetMaintenanceScheduleByIdQuery query) {
        return maintenanceScheduleRepository.findById(query.scheduleId());
    }

    @Override
    public Optional<MaintenanceSchedule> handle(GetMaintenanceScheduleByVehicleIdQuery query) {
        return maintenanceScheduleRepository.findByVehicleId(new VehicleId(query.vehicleId()));
    }

    @Override
    public List<MaintenanceSchedule> handle(GetMaintenanceScheduleDueSoonQuery query) {
        return maintenanceScheduleRepository.findByStatusAndNextEvaluationAtBefore(MaintenanceScheduleStatus.ACTIVE, query.dueBefore());
    }

    @Override
    public List<MaintenanceSchedule> handle(GetActiveMaintenanceSchedulesQuery query) {
        return maintenanceScheduleRepository.findByStatus(MaintenanceScheduleStatus.ACTIVE);
    }
}
