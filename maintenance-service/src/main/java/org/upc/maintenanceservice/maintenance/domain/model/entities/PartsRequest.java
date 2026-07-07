package org.upc.maintenanceservice.maintenance.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import org.upc.maintenanceservice.maintenance.domain.model.aggregates.MaintenanceOrder;
import org.upc.maintenanceservice.maintenance.domain.model.commands.RequestPartsCommand;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.PartsRequestStatus;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.Quantity;

@Entity
@Getter
public class PartsRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_order_id", nullable = false)
    private MaintenanceOrder maintenanceOrder;

    @Column(nullable = false)
    private String partName;

    @Embedded
    private Quantity quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartsRequestStatus status;

    protected PartsRequest() {
    }

    public PartsRequest(MaintenanceOrder maintenanceOrder, RequestPartsCommand command) {
        this.maintenanceOrder = maintenanceOrder;
        this.partName = command.partName();
        this.quantity = new Quantity(command.quantity());
        this.status = PartsRequestStatus.REQUESTED;
    }

    public void markAsReceived() {
        this.status = PartsRequestStatus.RECEIVED;
    }

    public boolean isReceived() {
        return this.status == PartsRequestStatus.RECEIVED;
    }
}
