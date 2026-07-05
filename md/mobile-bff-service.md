# Mobile BFF Service

## Que hace

El `mobile-bff-service` autoriza subidas directas de evidencias binarias a S3 sin pasar archivos por el backend.

Expone:

```http
POST /api/v1/mobile/evidence/upload-intents
POST /api/v1/mobile/evidence/upload-intents/{uploadIntentId}/confirm
```

El primer endpoint crea o reutiliza un `UploadIntent` idempotente usando `clientEvidenceId`, genera un `objectKey` deterministico y devuelve una URL presignada `PUT` para S3 con headers requeridos.

El segundo endpoint confirma la subida. No confia solo en el request del movil: valida el objeto con S3 `HeadObject`, comparando existencia, `Content-Length`, `Content-Type`, `x-amz-meta-client-evidence-id` y `x-amz-meta-sha256`.

Cuando la confirmacion es valida, marca el intent como `CONFIRMED` y registra un outbox local `EvidenceUploadConfirmed`. En Parte III, un relay programado publica ese evento hacia RabbitMQ local/demo para que `ai-validation-service` lo consuma asincronicamente.

El servicio no es duenio final de Evidence. Solo administra autorizacion y confirmacion de subida; `edge-service` sigue recibiendo la metadata offline y los eventos logisticos correlados por `clientEvidenceId`.

## Validaciones

Implementacion terminada, pendiente de validacion manual en un entorno con Java 21 y credenciales/configuracion S3.

Comandos recomendados:

```powershell
# 1. Verificar Java 21
java -version

# 2. Si hace falta, setear JAVA_HOME en PowerShell
$env:JAVA_HOME = "C:\Users\TU_USUARIO\.jdks\temurin-21.0.8"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# 3. Ejecutar tests del nuevo servicio
.\mvnw.cmd -pl mobile-bff-service -am test

# 4. Empaquetar el servicio
.\mvnw.cmd -pl mobile-bff-service -am package

# 5. Recompilar y levantar solo mobile-bff-service
docker compose -f docker-compose.yml up -d --build mobile-bff-service

# 6. Probar integracion con gateway/config/eureka
docker compose -f docker-compose.yml up -d --build config-service eureka-service mobile-bff-service gateway-service
```

Variables esperadas para S3:

```powershell
$env:AWS_REGION = "us-east-1"
$env:AWS_ACCESS_KEY_ID = "..."
$env:AWS_SECRET_ACCESS_KEY = "..."
$env:S3_EVIDENCE_BUCKET = "cobox-evidence-dev"
$env:S3_PRESIGNED_URL_EXPIRATION_MINUTES = "15"
```

Endpoints:

```text
POST http://localhost:8088/api/v1/mobile/evidence/upload-intents
POST http://localhost:8088/api/v1/mobile/evidence/upload-intents/{uploadIntentId}/confirm

POST http://localhost:8080/api/v1/mobile/evidence/upload-intents
POST http://localhost:8080/api/v1/mobile/evidence/upload-intents/{uploadIntentId}/confirm
```

## Deuda tecnica

- No pude verificar tests/build porque esta shell no tiene Java disponible (`java: command not found`) y `JAVA_HOME` no esta configurado.
- El relay de outbox publica hacia RabbitMQ local/demo, pero no hay estrategia productiva de broker administrado o alternativa cloud seleccionada.
- No hay validacion granular de permisos entre conductor, ruta y orden. El servicio usa JWT general y toma `driverId` desde claims si existen; si no, acepta `driverId` en el request como transicion v1.
- No hay integracion con `edge-service` para consultar o cerrar el estado final de la evidencia. La correlacion queda por `clientEvidenceId`.
- No hay endpoint administrativo para inspeccionar intents, confirmaciones u outbox.
- No hay expiracion automatica por scheduler para intents vencidos. El refresh ocurre cuando se reintenta crear con el mismo `clientEvidenceId`.
- La configuracion local usa S3 real. Para desarrollo sin AWS convendria agregar MinIO o LocalStack.
- Usa `ddl-auto: update`, consistente con el repo actual, pero para produccion convendria migraciones explicitas con Flyway/Liquibase.
- No se validan politicas avanzadas de S3 como SSE, KMS, lifecycle, bucket policy o bloqueo publico.
- La confirmacion valida metadata enviada en la subida, pero no calcula el hash desde el binario en backend porque el backend no descarga ni recibe archivos.

Nada de esto bloquea la Parte II-A; son deudas esperables para un primer corte de subida directa con URLs presignadas.


## Acotaciones adicionales
Sí, puedes proceder con la **Parte II-B**, pero dejaría estas acotaciones antes de avanzar:

## Acotaciones breves sobre Parte II-A

1. **Arquitectónicamente está correcto.**
   El flujo cumple el objetivo: el backend no recibe binarios, el móvil sube directo a S3 y el BFF solo autoriza/confirma.

2. **La mayor deuda a cerrar pronto es autorización fina.**
   Que el servicio acepte `driverId` desde el request si no viene en JWT está bien como transición v1, pero antes de demo seria conviene endurecerlo:

   ```text
   driverId debe venir del JWT o de una relación validada contra backend.
   El cliente móvil no debe poder autoasignarse otro driverId.
   ```

3. **Bien por validar con `HeadObject`.**
   Eso evita una confirmación falsa desde el móvil. La validación de `Content-Length`, `Content-Type`, `clientEvidenceId` y `sha256` como metadata es suficiente para este corte.

4. **La deuda del hash es aceptable.**
   No calcular SHA-256 desde backend es coherente con “no descargar binarios”. Para una versión más fuerte, el móvil debe calcular hash antes de subir y S3 debe recibirlo como metadata o checksum firmado.

5. **El outbox local ya conecta con Parte III.**
   `EvidenceUploadConfirmed` se publica hacia RabbitMQ local/demo y `ai-validation-service` lo consume asincronicamente.

6. **Cuidado con el ownership de Evidence.**
   La frase “el `mobile-bff-service` administra autorización y confirmación” está bien. Solo evita que más adelante empiece a concentrar reglas de dominio de evidencia. Cuando se implemente `evidence-service` completo, ese servicio debería ser el dueño final del estado de evidencia.

7. **Antes de probar con AWS real, revisa seguridad mínima del bucket.**

   ```text
   - Block Public Access activado.
   - Bucket privado.
   - CORS limitado a PUT y headers requeridos.
   - Expiración corta de presigned URL.
   - No subir credenciales AWS al repo.
   - Variables por entorno o secrets.
   ```

8. **Falta validación manual, pero no bloquea II-B.**
   Lo único que sí pediría antes de considerar cerrada la Parte II-A es ejecutar:

   ```powershell
   .\mvnw.cmd -pl mobile-bff-service -am test
   .\mvnw.cmd -pl mobile-bff-service -am package
   docker compose -f docker-compose.yml up -d --build config-service eureka-service mobile-bff-service gateway-service
   ```

---

## Dictamen

Puedes avanzar con **Parte II-B: `desktop-bff-service` agregador**.

Solo deja registrado que Parte II-A queda como:

```text
Estado: implementado, pendiente de validación manual con Java 21 + S3 real.
Bloqueante para II-B: no.
Bloqueante para producción/demo real: autorización fina driver-ruta-orden.
```

## Mini-ADR de cierre

**Decisión:** Aceptar `mobile-bff-service` como BFF de subida directa con S3 presigned URLs y confirmación vía `HeadObject`.
**Impacto en 12 semanas:** Medio, ya desbloquea evidencia binaria real e IA posterior.
**Costo de Nube:** Free Tier / bajo consumo S3 en entorno dev.
**Riesgo para el equipo:** Medio. Principal riesgo pendiente: autorización granular entre conductor, ruta y orden.
