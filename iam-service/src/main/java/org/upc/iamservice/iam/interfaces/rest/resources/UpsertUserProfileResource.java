package org.upc.iamservice.iam.interfaces.rest.resources;

public record UpsertUserProfileResource(String email, String firstName, String lastName, String phone) {
}
