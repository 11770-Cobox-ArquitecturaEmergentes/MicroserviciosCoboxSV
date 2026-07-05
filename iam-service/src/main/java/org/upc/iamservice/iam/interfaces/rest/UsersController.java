package org.upc.iamservice.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.upc.iamservice.iam.domain.model.queries.GetUserByAuth0SubjectQuery;
import org.upc.iamservice.iam.domain.model.queries.GetAllUsersQuery;
import org.upc.iamservice.iam.domain.model.queries.GetUserByIdQuery;
import org.upc.iamservice.iam.domain.services.UserCommandService;
import org.upc.iamservice.iam.domain.services.UserQueryService;
import org.upc.iamservice.iam.interfaces.rest.resources.UpsertUserProfileResource;
import org.upc.iamservice.iam.interfaces.rest.resources.UserResource;
import org.upc.iamservice.iam.interfaces.rest.transform.UpsertUserProfileCommandFromResourceAssembler;
import org.upc.iamservice.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "User Management Endpoints")
public class UsersController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public UsersController(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    @Operation(summary = "Create or update current user's internal profile",
            description = "Creates or updates the internal IAM profile linked to the Auth0 subject from the access token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserResource.class))),
                    @ApiResponse(responseCode = "201", description = "Profile created successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserResource.class)))
            })
    @PutMapping("/me")
    public ResponseEntity<UserResource> upsertCurrentUserProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpsertUserProfileResource resource
    ) {
        var existingUser = userQueryService.handle(new GetUserByAuth0SubjectQuery(jwt.getSubject()));
        var command = UpsertUserProfileCommandFromResourceAssembler.toCommandFromResource(jwt.getSubject(), resource);
        var user = userCommandService.handle(command);
        if (user.isEmpty()) return ResponseEntity.badRequest().build();
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return existingUser.isPresent()
                ? ResponseEntity.ok(userResource)
                : ResponseEntity.status(201).body(userResource);
    }

    @Operation(summary = "Get all users",
            description = "Retrieves a list of all registered users in the system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = UserResource.class)))),
                    @ApiResponse(responseCode = "500", description = "Internal server error",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @GetMapping
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var getAllUsersQuery = new GetAllUsersQuery();
        var users = userQueryService.handle(getAllUsersQuery);
        var userResources = users.stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(userResources);
    }

    @Operation(summary = "Get a user by ID",
            description = "Retrieves the details of a specific user using their unique identifier.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserResource.class))),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "500", description = "Internal server error",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResource> getUserById(@PathVariable Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var user = userQueryService.handle(getUserByIdQuery);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(userResource);
    }
}
