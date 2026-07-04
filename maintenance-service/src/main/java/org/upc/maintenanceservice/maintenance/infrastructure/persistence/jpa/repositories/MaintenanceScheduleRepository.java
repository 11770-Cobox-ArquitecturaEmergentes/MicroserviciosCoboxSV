package org.upc.maintenanceservice.maintenance.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.upc.maintenanceservice.maintenance.domain.model.aggregates.MaintenanceSchedule;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceScheduleStatus;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.VehicleId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Long> {
    Optional<MaintenanceSchedule> findByVehicleId(VehicleId vehicleId);

    List<MaintenanceSchedule> findByStatus(MaintenanceScheduleStatus status);

    List<MaintenanceSchedule> findByStatusAndNextEvaluationAtBefore(MaintenanceScheduleStatus status, LocalDateTime dueBefore);
}
