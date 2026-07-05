package org.upc.iamservice.iam.interfaces.acl;

import org.springframework.stereotype.Service;
import org.upc.iamservice.iam.domain.model.commands.UpsertUserProfileCommand;
import org.upc.iamservice.iam.domain.model.queries.GetUserByAuth0SubjectQuery;
import org.upc.iamservice.iam.domain.model.queries.GetUserByEmailQuery;
import org.upc.iamservice.iam.domain.model.queries.GetUserByIdQuery;
import org.upc.iamservice.iam.domain.services.UserCommandService;
import org.upc.iamservice.iam.domain.services.UserQueryService;
import org.upc.iamservice.iam.interfaces.rest.resources.UserResource;
import org.upc.iamservice.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;

import java.util.Objects;
import java.util.Optional;

@Service
public class IamContextFacade {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public IamContextFacade(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    public Long createUser(String auth0Subject, String email, String firstName, String lastName, String phone) {
        var command = new UpsertUserProfileCommand(auth0Subject, email, firstName, lastName, phone);
        var result = userCommandService.handle(command);
        if (result.isEmpty()) return 0L;
        return result.get().getId();
    }

    public Long fetchUserIdByAuth0Subject(String auth0Subject) {
        var query = new GetUserByAuth0SubjectQuery(auth0Subject);
        var result = userQueryService.handle(query);
        if (result.isEmpty()) return 0L;
        return result.get().getId();
    }

    public Optional<UserResource> fetchUserById(Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var result = userQueryService.handle(getUserByIdQuery);
        if(result.isEmpty()) return Optional.empty();
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(result.get());
        return Optional.of(userResource);
    }

    public Long fetchUserIdByEmail(String email) {
        var getUserByEmailQuery = new GetUserByEmailQuery(email);
        var result = userQueryService.handle(getUserByEmailQuery);
        if (result.isEmpty()) return 0L;
        return result.get().getId();
    }

    public boolean existsUserByEmailAndIdIsNot(String email, Long id) {
        var getUserByEmailQuery = new GetUserByEmailQuery(email);
        var result = userQueryService.handle(getUserByEmailQuery);
        if (result.isEmpty()) return false;
        return !Objects.equals(result.get().getId(), id);
    }

    public boolean existsUserById(Long id) {
        var getUserByIdQuery = new GetUserByIdQuery(id);
        var result = userQueryService.handle(getUserByIdQuery);
        return result.isPresent();
    }

    public String fetchEmailByUserId(Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var result = userQueryService.handle(getUserByIdQuery);
        if (result.isEmpty()) return "";
        return result.get().getEmail();
    }
    public boolean existsUserByRole(Long userId, String roleName) {
        var query = new GetUserByIdQuery(userId);
        var result = userQueryService.handle(query);
        if (result.isEmpty()) return false;

        return result.get().getRoles().stream()
                .anyMatch(role -> role.getStringName().equals(roleName));
    }
}
