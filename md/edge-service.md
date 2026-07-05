# Edge Service

## Que hace:

El nuevo `edge-service` es el punto backend para recibir sincronizaciones offline del móvil del conductor.

Expone:

```http
POST /api/v1/edge/sync-batches
```

Recibe un lote con:

- evidencias de entrega: `objectKey`, `sha256`, tipo, orden, ruta, timestamp.
- telemetría GPS básica: latitud, longitud, velocidad, batería, precisión.
- eventos offline: tipo, agregado, payload y fecha.

Su función principal es registrar esos datos de forma idempotente. El móvil manda UUIDs propios como `clientBatchId`, `clientEvidenceId`, `clientTelemetryId` y `clientEventId`; si reintenta por mala conexión, el backend detecta duplicados y no vuelve a insertar lo mismo.

También permite aceptación parcial: si un lote trae algunos ítems válidos y otros inválidos, guarda los válidos y responde cuáles fueron `RECORDED`, `DUPLICATE` o `REJECTED`.

No recibe archivos binarios todavía. Solo guarda metadata y `objectKey`, dejando listo el camino para que en la Parte II el móvil suba archivos directo al storage usando URLs firmadas.

## Validaciones
Implementación terminada, las validaciones se hace de forma manual.

Comandos recomendados:

```powershell
# 1. Verificar Java 21
java -version

# 2. Si hace falta, setear JAVA_HOME en PowerShell
$env:JAVA_HOME = "C:\Users\TU_USUARIO\.jdks\temurin-21.0.8"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# 3. Ejecutar tests del nuevo servicio
.\mvnw.cmd -pl edge-service -am test

# 4. Empaquetar el servicio
.\mvnw.cmd -pl edge-service -am package

# 5. Recompilar y levantar solo edge-service
docker compose -f docker-compose.yml up -d --build edge-service

# 6. Si quieres probar integración con gateway/config/eureka
docker compose -f docker-compose.yml up -d --build config-service eureka-service edge-service gateway-service
```

Para validar salud:

```powershell
curl http://localhost:8087/actuator/health
```

Si levantas vía gateway, el endpoint nuevo queda en:

```text
POST http://localhost:8080/api/v1/edge/sync-batches
```

Recuerda que está protegido por JWT como los demás servicios, salvo `/actuator/**` y Swagger.

## Deuda tecnica

- No pude verificar tests/build por el problema de `JAVA_HOME`; queda pendiente ejecutar `.\mvnw.cmd -pl edge-service -am test`.
- El resultado por ítem rechazado no se persiste. Si un lote parcialmente aceptado se reenvía con el mismo `clientBatchId`, la respuesta `DUPLICATE` reconstruye solo los ítems guardados, no los rechazados originalmente.
- Usa `ddl-auto: update`, consistente con el repo actual, pero para producción convendría migraciones explícitas con Flyway/Liquibase.
- No hay outbox/event bus todavía. El `edge-service` registra datos, pero no publica eventos para IA o analítica.
- No hay autorización granular por rol/conductor; solo validación JWT general.
- No hay endpoint de consulta/admin para inspeccionar lotes sincronizados, evidencias o telemetría.

Nada de esto bloquea la Parte I; son deudas esperables para un primer corte backend. Podemos continuar con la planificación de la Parte II: BFF móvil, URLs firmadas y confirmación de subida directa a storage.