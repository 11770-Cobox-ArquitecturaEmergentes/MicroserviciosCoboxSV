package org.upc.iamservice.iam.domain.services;

import org.upc.iamservice.iam.domain.model.aggregates.User;
import org.upc.iamservice.iam.domain.model.commands.UpsertUserProfileCommand;

import java.util.Optional;

public interface UserCommandService {
    Optional<User> handle(UpsertUserProfileCommand command);
}
