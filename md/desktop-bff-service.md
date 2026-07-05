# Desktop BFF Service

## Que hace

El `desktop-bff-service` es un BFF de solo lectura para el cliente web gerencial/operativo.

Expone vistas agregadas:

```http
GET /api/v1/desktop/dashboard/operations
GET /api/v1/desktop/routes/{routeId}/overview
GET /api/v1/desktop/vehicles/{vehicleId}/health
```

Consume microservicios internos por Feign:

- `fleet-service`
- `delivery-service`
- `incident-service`
- `maintenance-service`

No persiste datos, no ejecuta comandos de dominio y no consulta bases de datos internas. Su responsabilidad es componer vistas para escritorio y aislar al cliente web de los contratos internos.

Todas las respuestas incluyen `generatedAt` y `degradedSections`. Si una dependencia secundaria falla, el BFF responde `200 OK` con datos parciales y registra la seccion degradada. Si falta el recurso principal de una vista, responde `404`.

## Validaciones

Implementacion terminada, pendiente de validacion manual en un entorno con Java 21.

Comandos recomendados:

```powershell
# 1. Verificar Java 21
java -version

# 2. Si hace falta, setear JAVA_HOME en PowerShell
$env:JAVA_HOME = "C:\Users\TU_USUARIO\.jdks\temurin-21.0.8"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# 3. Ejecutar tests del nuevo servicio
.\mvnw.cmd -pl desktop-bff-service -am test

# 4. Empaquetar el servicio
.\mvnw.cmd -pl desktop-bff-service -am package

# 5. Recompilar y levantar solo desktop-bff-service
docker compose -f docker-compose.yml up -d --build desktop-bff-service

# 6. Probar integracion con gateway/config/eureka y servicios de dominio
docker compose -f docker-compose.yml up -d --build config-service eureka-service fleet-service delivery-service incident-service maintenance-service desktop-bff-service gateway-service
```

Endpoints locales:

```text
GET http://localhost:8089/api/v1/desktop/dashboard/operations
GET http://localhost:8089/api/v1/desktop/routes/{routeId}/overview
GET http://localhost:8089/api/v1/desktop/vehicles/{vehicleId}/health

GET http://localhost:8080/api/v1/desktop/dashboard/operations
GET http://localhost:8080/api/v1/desktop/routes/{routeId}/overview
GET http://localhost:8080/api/v1/desktop/vehicles/{vehicleId}/health
```

## Deuda tecnica

- No pude verificar tests/build porque esta shell no tiene Java disponible (`java: command not found`) y `JAVA_HOME` no esta configurado.
- No hay cache. Cada request del BFF consulta los microservicios internos.
- No hay autorizacion granular por rol gerencial/operativo; mantiene validacion JWT general como los servicios actuales.
- No hay circuit breakers ni timeouts dedicados por dependencia. La degradacion se maneja capturando fallas de Feign.
- `vehicle health` marca `maintenance.schedules` como degradado porque `maintenance-service` aun no expone un endpoint de schedules por `vehicleId`.
- El dashboard de mantenimiento se calcula consultando estados `OPEN`, `SCHEDULED` e `IN_PROGRESS`; no hay endpoint interno de "all maintenance orders".
- No hay paginacion ni filtros todavia; las vistas usan los endpoints de lista existentes.
- No hay versionado del contrato del BFF mas alla de `/api/v1`.

Nada de esto bloquea la Parte II-B; son deudas esperables para un primer corte de agregacion de solo lectura.
