package org.upc.desktopbffservice.desktop.infrastructure.clients.delivery;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "delivery-service")
public interface DeliveryClient {

    @GetMapping("/api/v1/orders")
    List<OrderClientResource> getOrders();

    @GetMapping("/api/v1/orders/{orderId}")
    OrderClientResource getOrderById(@PathVariable Long orderId);
}
