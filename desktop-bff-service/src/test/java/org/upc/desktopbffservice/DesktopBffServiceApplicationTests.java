package org.upc.desktopbffservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.upc.desktopbffservice.desktop.infrastructure.clients.delivery.DeliveryClient;
import org.upc.desktopbffservice.desktop.infrastructure.clients.fleet.FleetClient;
import org.upc.desktopbffservice.desktop.infrastructure.clients.incident.IncidentClient;
import org.upc.desktopbffservice.desktop.infrastructure.clients.maintenance.MaintenanceClient;

@SpringBootTest
class DesktopBffServiceApplicationTests {

    @MockBean
    private FleetClient fleetClient;

    @MockBean
    private DeliveryClient deliveryClient;

    @MockBean
    private IncidentClient incidentClient;

    @MockBean
    private MaintenanceClient maintenanceClient;

    @Test
    void contextLoads() {
    }
}
