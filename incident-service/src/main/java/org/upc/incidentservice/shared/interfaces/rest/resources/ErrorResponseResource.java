package org.upc.incidentservice.shared.interfaces.rest.resources;

import java.time.LocalDateTime;

public record ErrorResponseResource(LocalDateTime timestamp, String message, String path) {
}
