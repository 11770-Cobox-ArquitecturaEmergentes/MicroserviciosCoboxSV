# Contexto de `incident` y `maintenance`

## 1) Resumen corto

Estos dos módulos representan dos bounded contexts distintos:

- `incident`: gestión de incidencias operativas.
- `maintenance`: gestión de mantenimiento preventivo/correctivo/predictivo, órdenes, schedules, jobs, repuestos y costos.

Ambos usan Spring Boot + JPA + REST, con una arquitectura por capas y separación entre `domain`, `application`, `infrastructure` e `interfaces`.

---

## 2) Módulo `incident`

### Qué hace

Gestiona incidencias:

- crear una incidencia
- cambiar su estado
- asignar un responsable
- consultar incidencias

### Ruta base

`/api/v1/incidents`

### Estructura real

- `application/internal/commandservices`
- `application/internal/queryservices`
- `domain/model/aggregates`
- `domain/model/commands`
- `domain/model/events`
- `domain/model/queries`
- `domain/model/valueobjects`
- `domain/services`
- `infrastructure/persistence/jpa/repositories`
- `interfaces/rest`
- `interfaces/rest/resources`
- `interfaces/rest/transform`

### Agregado principal

`Incident`

Campos principales:

- `id` técnico (`Long`)
- `incidentId` de negocio (`UUID`)
- `type`
- `description`
- `reportedAt`
- `severity`
- `status`
- `responsibleUserId` opcional

### Reglas de negocio

- Al crear:
  - si `severity = CRITICAL`, el estado inicial es `ESCALATED`
  - en otro caso, `OPEN`
- `updateStatus` solo permite transiciones válidas según el estado actual.
- `assignResponsible` es idempotente si ya tenía el mismo responsable.

### Estados

- `OPEN`
- `IN_PROGRESS`
- `ESCALATED`
- `RESOLVED`
- `CLOSED`

### Eventos de dominio

- `IncidentReportedEvent`
- `IncidentStatusUpdatedEvent`
- `IncidentAssignedEvent`

### REST contract

- `POST /api/v1/incidents`
- `GET /api/v1/incidents`
- `GET /api/v1/incidents/{id}` donde `{id}` es UUID
- `PATCH /api/v1/incidents/{id}/status`
- `PATCH /api/v1/incidents/{id}/assign`

### Persistencia

Repositorio:

- `IncidentRepository extends JpaRepository<Incident, Long>`

Búsquedas:

- `findByIncidentId(IncidentId)`
- `findByStatus(IncidentStatus)`
- `findByResponsibleUserId(ResponsibleUserId)`

### DTOs REST

- `CreateIncidentResource`
- `UpdateIncidentStatusResource`
- `AssignResponsibleUserResource`
- `IncidentResource`

### Mapeadores

- `CreateIncidentCommandFromResourceAssembler`
- `UpdateIncidentStatusCommandFromResourceAssembler`
- `AssignResponsibleUserCommandFromResourceAssembler`
- `IncidentResourceFromEntityAssembler`

---

## 3) Módulo `maintenance`

### Qué hace

Gestiona el ciclo completo de mantenimiento de vehículos:

- schedules de mantenimiento
- órdenes de mantenimiento
- activación/desactivación de schedules
- evaluación de reglas
- jobs
- repuestos
- costos
- cierre/cancelación de órdenes
- publicación de eventos a outbox

### Rutas base

- `/api/v1/maintenance-schedules`
- `/api/v1/maintenance-orders`

### Estructura real

- `application/internal/commandservices`
- `application/internal/queryservices`
- `application/internal/eventhandlers`
- `domain/model/aggregates`
- `domain/model/commands`
- `domain/model/entities`
- `domain/model/events`
- `domain/model/queries`
- `domain/model/valueobjects`
- `domain/services`
- `infrastructure/persistence/jpa/repositories`
- `infrastructure/outbox`
- `interfaces/rest`
- `interfaces/rest/resources`
- `interfaces/rest/transform`

### Agregados principales

#### `MaintenanceOrder`

Campos:

- `vehicleId`
- `maintenanceType`
- `priority`
- `status`
- `reason`
- `openingOdometer`
- `closingOdometer`
- `scheduledTimelapse`
- `jobs`
- `partsRequests`
- `totalCost`
- `technicianId`

Estados:

- `OPEN`
- `SCHEDULED`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

#### `MaintenanceSchedule`

Campos:

- `vehicleId`
- `status`
- `rules`
- `lastEvaluationAt`
- `nextEvaluationAt`

### Reglas de negocio clave

#### Orders

- no puede haber más de una orden abierta por vehículo
- solo se puede programar desde `OPEN`
- solo se puede iniciar desde `SCHEDULED`
- solo se puede completar desde `IN_PROGRESS`
- para completar:
  - todos los jobs deben estar completos
  - todos los repuestos deben estar recibidos
  - `closingOdometer >= openingOdometer`
- no se puede cancelar si ya está `COMPLETED`
- `registerCost` exige misma moneda

#### Schedules

- un schedule puede activarse o desactivarse
- las reglas no pueden quedar vacías
- evaluar schedule puede generar órdenes sugeridas según reglas y odómetro/fecha

### Entidades internas

- `Job`
- `PartsRequest`

### Value objects / enums

- `MaintenanceRule`
- `VehicleId`
- `Odometer`
- `Timelapse`
- `Money`
- `Quantity`
- `ChecklistItem`
- `MaintenanceTypes`
- `Priorities`
- `Reason`
- `MaintenanceOrderStatus`
- `PartsRequestStatus`

### Comandos

#### Orders

- `CreateMaintenanceOrderCommand`
- `ScheduleMaintenanceOrderCommand`
- `StartMaintenanceOrderCommand`
- `CompleteMaintenanceOrderCommand`
- `CancelMaintenanceOrderCommand`
- `RegisterJobCommand`
- `RequestPartsCommand`
- `ReceivePartsCommand`
- `RegisterCostCommand`

#### Schedules

- `CreateMaintenanceScheduleCommand`
- `ActivateMaintenanceScheduleCommand`
- `DeactivateMaintenanceScheduleCommand`
- `EvaluateMaintenanceScheduleCommand`
- `UpdateMaintenanceRulesCommand`

### Queries

#### Orders

- `GetMaintenanceOrderByIdQuery`
- `GetMaintenanceOrderHistoryQuery`
- `GetOpenMaintenanceOrdersByVehicleIdQuery`
- `GetMaintenanceOrdersByStatusQuery`
- `HasMaintenanceOpenOrderForVehicleIdQuery`

#### Schedules

- `GetMaintenanceScheduleByIdQuery`
- `GetMaintenanceScheduleByVehicleIdQuery`
- `GetMaintenanceScheduleDueSoonQuery`
- `GetActiveMaintenanceSchedulesQuery`

### Eventos de dominio

- `MaintenanceOrderCreatedEvent`
- `MaintenanceOrderScheduledEvent`
- `MaintenanceOrderStartedEvent`
- `MaintenanceOrderCompletedEvent`
- `MaintenanceOrderCancelledEvent`
- `PartsRequestedEvent`
- `PartsReceivedEvent`
- `ScheduleActivatedEvent`
- `ScheduleDeactivatedEvent`
- `RulesUpdatedEvent`
- `ThresholdReachedEvent`

### REST contract

#### Schedules

- `POST /api/v1/maintenance-schedules`
- `POST /api/v1/maintenance-schedules/{id}/activate`
- `POST /api/v1/maintenance-schedules/{id}/deactivate`
- `POST /api/v1/maintenance-schedules/{id}/evaluate`
- `PUT /api/v1/maintenance-schedules/{id}/rules`
- `GET /api/v1/maintenance-schedules/{id}`

#### Orders

- `POST /api/v1/maintenance-orders`
- `POST /api/v1/maintenance-orders/{id}/schedule`
- `POST /api/v1/maintenance-orders/{id}/start`
- `POST /api/v1/maintenance-orders/{id}/complete`
- `POST /api/v1/maintenance-orders/{id}/cancel`
- `POST /api/v1/maintenance-orders/{id}/jobs`
- `POST /api/v1/maintenance-orders/{id}/parts/request`
- `POST /api/v1/maintenance-orders/{id}/parts/receive`
- `POST /api/v1/maintenance-orders/{id}/cost`
- `GET /api/v1/maintenance-orders/{id}`
- `GET /api/v1/maintenance-orders/status/{status}`
- `GET /api/v1/maintenance-orders/vehicle/{vehicleId}/open`
- `GET /api/v1/maintenance-orders/vehicle/{vehicleId}/has-open`
- `GET /api/v1/maintenance-orders/vehicle/{vehicleId}/history`

### Persistencia

Repositorios:

- `MaintenanceOrderRepository`
- `MaintenanceScheduleRepository`

### Outbox

Hay una implementación de outbox para eventos de integración:

- `OutboxMessage`
- `OutboxRepository`
- `OutboxRelay`
- `IntegrationEventPublisher`

Esto permite persistir eventos como mensajes pendientes de publicación.

### DTOs REST

Schedules:

- `CreateMaintenanceScheduleResource`
- `ActivateMaintenanceScheduleResource`
- `DeactivateMaintenanceScheduleResource`
- `EvaluateMaintenanceScheduleResource`
- `UpdateMaintenanceRulesResource`
- `MaintenanceScheduleResource`

Orders:

- `CreateMaintenanceOrderResource`
- `ScheduleMaintenanceOrderResource`
- `StartMaintenanceOrderResource`
- `CompleteMaintenanceOrderResource`
- `CancelMaintenanceOrderResource`
- `RegisterJobResource`
- `RequestPartsResource`
- `ReceivePartsResource`
- `RegisterCostResource`
- `MaintenanceOrderResource`

### Mapeadores

- `CreateMaintenanceScheduleCommandFromResourceAssembler`
- `CreateMaintenanceOrderCommandFromResourceAssembler`
- `ActivateMaintenanceScheduleCommandFromResourceAssembler`
- `DeactivateMaintenanceScheduleCommandFromResourceAssembler`
- `EvaluateMaintenanceScheduleCommandFromResourceAssembler`
- `UpdateMaintenanceRulesCommandFromResourceAssembler`
- `ScheduleMaintenanceOrderCommandFromResourceAssembler`
- `StartMaintenanceOrderCommandFromResourceAssembler`
- `CompleteMaintenanceOrderCommandFromResourceAssembler`
- `CancelMaintenanceOrderCommandFromResourceAssembler`
- `RegisterJobCommandFromResourceAssembler`
- `RequestPartsCommandFromResourceAssembler`
- `ReceivePartsCommandFromResourceAssembler`
- `RegisterCostCommandFromResourceAssembler`
- `MaintenanceScheduleResourceFromEntityAssembler`
- `MaintenanceOrderResourceFromEntityAssembler`
- `MaintenanceRuleFromResourceAssembler`
- `MaintenanceRuleResourceFromEntityAssembler`
- `JobResourceFromEntityAssembler`
- `PartsRequestResourceFromEntityAssembler`

---

## 4) Prompt para el otro backend

```text
Recrea dos módulos/bounded contexts: Incident y Maintenance.

INCIDENT
- Entidad/agregado Incident con:
  - id técnico interno
  - business id UUID
  - type, description, reportedAt, severity, status, responsibleUserId opcional
- Reglas:
  - al crear: si severity = CRITICAL, status = ESCALATED; si no, OPEN
  - transiciones válidas:
    - OPEN -> IN_PROGRESS/ESCALATED/CLOSED
    - IN_PROGRESS -> ESCALATED/RESOLVED/CLOSED
    - ESCALATED -> IN_PROGRESS/RESOLVED/CLOSED
    - RESOLVED -> CLOSED
    - CLOSED no cambia
  - asignar responsable es idempotente
- Endpoints:
  - POST /api/v1/incidents
  - GET /api/v1/incidents
  - GET /api/v1/incidents/{id} usando UUID
  - PATCH /api/v1/incidents/{id}/status
  - PATCH /api/v1/incidents/{id}/assign
- Persistencia:
  - repo por business id, status y responsibleUserId
- Eventos:
  - IncidentReported
  - IncidentStatusUpdated
  - IncidentAssigned

MAINTENANCE
- Crear dos agregados:
  1. MaintenanceSchedule
     - vehicleId, status ACTIVE/INACTIVE, rules, lastEvaluationAt, nextEvaluationAt
     - activar, desactivar, actualizar reglas, evaluar
  2. MaintenanceOrder
     - vehicleId, maintenanceType, priority, status, reason, openingOdometer, closingOdometer, scheduledTimelapse, jobs, partsRequests, totalCost, technicianId
     - estados: OPEN, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
     - reglas:
       - no duplicar órdenes abiertas por vehículo
       - solo schedule desde OPEN
       - solo start desde SCHEDULED
       - solo complete desde IN_PROGRESS
       - complete exige jobs completos, repuestos recibidos y closingOdometer >= openingOdometer
       - cancel no debe permitir cancelar COMPLETED
       - register cost exige misma moneda
- Entidades internas:
  - Job
  - PartsRequest
- Value objects/enums:
  - MaintenanceRule, VehicleId, Odometer, Timelapse, Money, Quantity, ChecklistItem
  - MaintenanceTypes, Priorities, Reason, MaintenanceOrderStatus, PartsRequestStatus
- Endpoints:
  - schedules: POST /api/v1/maintenance-schedules, POST /{id}/activate, POST /{id}/deactivate, POST /{id}/evaluate, PUT /{id}/rules, GET /{id}
  - orders: POST /api/v1/maintenance-orders, POST /{id}/schedule, POST /{id}/start, POST /{id}/complete, POST /{id}/cancel, POST /{id}/jobs, POST /{id}/parts/request, POST /{id}/parts/receive, POST /{id}/cost, GET /{id}, GET /status/{status}, GET /vehicle/{vehicleId}/open, GET /vehicle/{vehicleId}/has-open, GET /vehicle/{vehicleId}/history
- Persistencia:
  - repositorios para orders y schedules
- Eventos:
  - MaintenanceOrderCreated, Scheduled, Started, Completed, Cancelled
  - PartsRequested, PartsReceived
  - ScheduleActivated, ScheduleDeactivated, RulesUpdated
  - ThresholdReached
- Si implementas integración asíncrona, usa outbox.
```

---

## 5) Nota importante

En este repo hay algunas diferencias de casing y nombres de paquetes entre `Incident` y `incident`. Para migrarlo a otro backend, conviene normalizar nombres de paquetes y mantener solo el contrato funcional.
