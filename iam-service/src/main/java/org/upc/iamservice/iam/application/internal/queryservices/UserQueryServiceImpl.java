package org.upc.iamservice.iam.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.upc.iamservice.iam.domain.model.aggregates.User;
import org.upc.iamservice.iam.domain.model.queries.GetAllUsersQuery;
import org.upc.iamservice.iam.domain.model.queries.GetUserByAuth0SubjectQuery;
import org.upc.iamservice.iam.domain.model.queries.GetUserByEmailQuery;
import org.upc.iamservice.iam.domain.model.queries.GetUserByIdQuery;
import org.upc.iamservice.iam.domain.services.UserQueryService;
import org.upc.iamservice.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> handle(GetAllUsersQuery query) {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId());
    }

    @Override
    public Optional<User> handle(GetUserByAuth0SubjectQuery query) {
        return userRepository.findByAuth0Subject(query.auth0Subject());
    }

    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmail(query.email());
    }
}
