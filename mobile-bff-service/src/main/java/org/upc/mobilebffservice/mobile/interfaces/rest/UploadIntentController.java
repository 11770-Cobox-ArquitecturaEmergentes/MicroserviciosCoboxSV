package org.upc.mobilebffservice.mobile.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.upc.mobilebffservice.mobile.application.internal.commandservices.UploadIntentCommandService;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.ConfirmUploadIntentResource;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.CreateUploadIntentResource;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.UploadConfirmationResource;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.UploadIntentResource;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/mobile/evidence/upload-intents", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Mobile Evidence Uploads", description = "Mobile BFF endpoints for direct evidence uploads")
public class UploadIntentController {

    private final UploadIntentCommandService uploadIntentCommandService;

    public UploadIntentController(UploadIntentCommandService uploadIntentCommandService) {
        this.uploadIntentCommandService = uploadIntentCommandService;
    }

    @Operation(summary = "Create an evidence upload intent",
            description = "Creates or reuses an idempotent upload intent and returns a S3 presigned PUT URL.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Upload intent created",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UploadIntentResource.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid upload intent")
            })
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadIntentResource> create(@RequestBody(required = false) CreateUploadIntentResource resource,
                                                       @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
        var response = uploadIntentCommandService.create(resource, resolveDriverId(jwt, resource));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Confirm an evidence upload intent",
            description = "Validates the uploaded object against S3 metadata and creates EvidenceUploadConfirmed in the local outbox.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Upload confirmed",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UploadConfirmationResource.class))),
                    @ApiResponse(responseCode = "409", description = "S3 object validation failed")
            })
    @PostMapping(value = "/{uploadIntentId}/confirm", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadConfirmationResource> confirm(@PathVariable UUID uploadIntentId,
                                                              @RequestBody(required = false) ConfirmUploadIntentResource resource) {
        return ResponseEntity.ok(uploadIntentCommandService.confirm(uploadIntentId, resource));
    }

    private Long resolveDriverId(Jwt jwt, CreateUploadIntentResource resource) {
        var fromJwt = readLongClaim(jwt, "driverId");
        if (fromJwt != null) return fromJwt;
        fromJwt = readLongClaim(jwt, "driver_id");
        if (fromJwt != null) return fromJwt;
        fromJwt = readLongClaim(jwt, "https://coboxsv.dev/driver_id");
        if (fromJwt != null) return fromJwt;
        return resource == null ? null : resource.driverId();
    }

    private Long readLongClaim(Jwt jwt, String claimName) {
        if (jwt == null || !jwt.hasClaim(claimName)) {
            return null;
        }
        var claim = jwt.getClaim(claimName);
        if (claim instanceof Number number) {
            return number.longValue();
        }
        if (claim instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
