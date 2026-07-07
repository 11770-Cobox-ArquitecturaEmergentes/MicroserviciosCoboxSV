package org.upc.maintenanceservice.maintenance.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.upc.maintenanceservice.maintenance.domain.model.aggregates.MaintenanceOrder;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceOrderStatus;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.VehicleId;

import java.util.Collection;
import java.util.List;

@Repository
public interface MaintenanceOrderRepository extends JpaRepository<MaintenanceOrder, Long> {
    List<MaintenanceOrder> findByVehicleId(VehicleId vehicleId);

    List<MaintenanceOrder> findByVehicleIdOrderByCreatedAtDesc(VehicleId vehicleId);

    List<MaintenanceOrder> findByStatus(MaintenanceOrderStatus status);

    List<MaintenanceOrder> findByVehicleIdAndStatusIn(VehicleId vehicleId, Collection<MaintenanceOrderStatus> statuses);

    boolean existsByVehicleIdAndStatusIn(VehicleId vehicleId, Collection<MaintenanceOrderStatus> statuses);
}
