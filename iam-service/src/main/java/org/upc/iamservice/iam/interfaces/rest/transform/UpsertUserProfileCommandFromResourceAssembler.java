package org.upc.iamservice.iam.interfaces.rest.transform;

import org.upc.iamservice.iam.domain.model.commands.UpsertUserProfileCommand;
import org.upc.iamservice.iam.interfaces.rest.resources.UpsertUserProfileResource;

public class UpsertUserProfileCommandFromResourceAssembler {
    public static UpsertUserProfileCommand toCommandFromResource(String auth0Subject, UpsertUserProfileResource resource) {
        return new UpsertUserProfileCommand(
                auth0Subject,
                resource.email(),
                resource.firstName(),
                resource.lastName(),
                resource.phone()
        );
    }
}
