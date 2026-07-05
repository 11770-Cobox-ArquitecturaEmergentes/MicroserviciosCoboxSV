# CoBox Smart Vision - Container View

## Mobile Evidence Upload Flow

The mobile app does not upload evidence binaries through backend services.

1. Mobile requests an upload intent through `Gateway -> mobile-bff-service`.
2. `mobile-bff-service` creates an idempotent `UploadIntent`, generates an S3 object key and returns a presigned `PUT` URL.
3. Mobile uploads the binary directly to S3 using the required signed headers.
4. Mobile confirms the upload through `Gateway -> mobile-bff-service`.
5. `mobile-bff-service` validates the uploaded object with S3 `HeadObject`.
6. `mobile-bff-service` records `EvidenceUploadConfirmed` in its local outbox for future async AI processing.
7. Mobile continues synchronizing offline metadata and events through `Gateway -> edge-service`.

```text
Mobile App
  | POST /api/v1/mobile/evidence/upload-intents
  v
Gateway
  v
mobile-bff-service
  | presign PUT
  v
Amazon S3

Mobile App
  | PUT binary evidence using presigned URL
  v
Amazon S3

Mobile App
  | POST /api/v1/mobile/evidence/upload-intents/{id}/confirm
  v
Gateway
  v
mobile-bff-service
  | HeadObject validation
  v
Amazon S3

mobile-bff-service
  | local outbox: EvidenceUploadConfirmed
  v
AI services - future async consumer

Mobile App
  | POST /api/v1/edge/sync-batches
  v
Gateway
  v
edge-service
```

## Ownership Boundary

`mobile-bff-service` owns upload authorization and confirmation only. It does not become the source of truth for logistics evidence. `edge-service` remains responsible for offline synchronization metadata, and future async consumers will correlate records by `clientEvidenceId`.
