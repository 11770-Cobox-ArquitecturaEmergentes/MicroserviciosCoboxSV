package org.upc.iamservice.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.upc.iamservice.iam.domain.model.aggregates.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAuth0Subject(String auth0Subject);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
