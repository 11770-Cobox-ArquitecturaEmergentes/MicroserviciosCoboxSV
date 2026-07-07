package org.upc.fleetservice.fleet.interfaces.rest.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.upc.fleetservice.fleet.domain.exceptions.*;
import org.upc.fleetservice.shared.interfaces.rest.resources.ErrorResponseResource;


import java.time.LocalDateTime;

@RestControllerAdvice // Anotación clave para que Spring lo reconozca como manejador global
public class FleetControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseResource> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        var message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
                .orElse("Validation error");

        var errorResponse = new ErrorResponseResource(
                LocalDateTime.now(),
                message,
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Manejadores para entidades no encontradas (404 Not Found)
    @ExceptionHandler({
            DriverNotFoundException.class,
            VehicleNotFoundException.class,
            RouteNotFoundException.class,
            OrderNotFoundException.class
    })
    public ResponseEntity<ErrorResponseResource> handleNotFoundException(RuntimeException ex, WebRequest request) {
        var errorResponse = new ErrorResponseResource(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Manejadores para conflictos de estado del negocio (409 Conflict)
    @ExceptionHandler({
            VehicleNotOperationalException.class,
            VehicleNotInRouteException.class,
            InvalidVehicleStateTransitionException.class,
            DriverNotAvailableException.class,
            DriverAlreadyExistsException.class,
            VehicleCapacityExceededException.class,
            RouteNotInProgressException.class,
            RouteNotPlannedException.class,
            RouteAlreadyAssignedDriverException.class,
            RouteAlreadyAssignedVehicleException.class
    })
    public ResponseEntity<ErrorResponseResource> handleBusinessStateConflictException(RuntimeException ex, WebRequest request) {
        var errorResponse = new ErrorResponseResource(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // Manejador genérico para cualquier otra excepción no controlada (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseResource> handleGlobalException(Exception ex, WebRequest request) {
        var errorResponse = new ErrorResponseResource(
                LocalDateTime.now(),
                "Ocurrió un error inesperado en el contexto fleet",
                request.getDescription(false) + " | Details: " + ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
