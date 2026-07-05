package org.upc.iamservice.iam.domain.services;

import org.upc.iamservice.iam.domain.model.aggregates.User;
import org.upc.iamservice.iam.domain.model.queries.GetAllUsersQuery;
import org.upc.iamservice.iam.domain.model.queries.GetUserByAuth0SubjectQuery;
import org.upc.iamservice.iam.domain.model.queries.GetUserByEmailQuery;
import org.upc.iamservice.iam.domain.model.queries.GetUserByIdQuery;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {
    List<User> handle(GetAllUsersQuery query);
    Optional<User> handle(GetUserByIdQuery query);
    Optional<User> handle(GetUserByAuth0SubjectQuery query);
    Optional<User> handle(GetUserByEmailQuery query);
}
