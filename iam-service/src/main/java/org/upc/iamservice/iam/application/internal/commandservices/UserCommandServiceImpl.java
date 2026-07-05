package org.upc.iamservice.iam.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.upc.iamservice.iam.domain.model.aggregates.User;
import org.upc.iamservice.iam.domain.model.commands.UpsertUserProfileCommand;
import org.upc.iamservice.iam.domain.model.valueobjects.Roles;
import org.upc.iamservice.iam.domain.services.UserCommandService;
import org.upc.iamservice.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.upc.iamservice.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<User> handle(UpsertUserProfileCommand command) {
        var existingUserBySubject = userRepository.findByAuth0Subject(command.auth0Subject());
        if (existingUserBySubject.isPresent()) {
            var user = existingUserBySubject.get();
            ensureEmailAvailableFor(command.email(), user.getId());
            user.updateUserInfo(command.email(), command.firstName(), command.lastName(), command.phone());
            return Optional.of(userRepository.save(user));
        }

        if (userRepository.existsByEmail(command.email()))
            throw new RuntimeException("Email already exists");

        var clientRole = roleRepository.findByName(Roles.ROLE_DRIVER)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        var user = new User(
                command.auth0Subject(),
                command.email(),
                command.firstName(),
                command.lastName(),
                command.phone(),
                java.util.List.of(clientRole)
        );
        return Optional.of(userRepository.save(user));
    }

    private void ensureEmailAvailableFor(String email, Long userId) {
        userRepository.findByEmail(email)
                .filter(user -> !user.getId().equals(userId))
                .ifPresent(user -> {
                    throw new RuntimeException("Email already exists");
                });
    }
}
