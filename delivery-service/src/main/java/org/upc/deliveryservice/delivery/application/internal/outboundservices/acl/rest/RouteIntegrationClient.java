package org.upc.deliveryservice.delivery.application.internal.outboundservices.acl.rest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.upc.deliveryservice.delivery.application.internal.outboundservices.acl.resource.AddOrderDeliveredResource;


@FeignClient(name = "fleet-service")
public interface RouteIntegrationClient {

    @PostMapping(
            value = "/api/v1/routes/{routeId}/delivered-orders",
            consumes = "application/json"
    )
    void addDeliveredOrderToRoute(
            @PathVariable Long routeId,
            @RequestBody AddOrderDeliveredResource resource
    );
}
