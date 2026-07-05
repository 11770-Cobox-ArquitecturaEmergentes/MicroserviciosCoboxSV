package org.upc.iamservice.iam.interfaces.rest.resources;

import java.util.List;

public record UserResource(Long id, String auth0Subject, String email, String firstName, String lastName, String phone, List<String> roles) {
}
