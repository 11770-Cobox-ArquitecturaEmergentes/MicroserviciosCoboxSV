package org.upc.iamservice.iam.domain.model.commands;

public record UpsertUserProfileCommand(
        String auth0Subject,
        String email,
        String firstName,
        String lastName,
        String phone) {
}
