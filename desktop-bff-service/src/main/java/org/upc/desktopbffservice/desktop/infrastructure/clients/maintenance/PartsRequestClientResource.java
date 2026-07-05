package org.upc.desktopbffservice.desktop.infrastructure.clients.maintenance;

public record PartsRequestClientResource(Long id, String partName, Integer quantity, String status) {
}
