package org.upc.iamservice.iam.domain.model.aggregates;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.upc.iamservice.iam.domain.model.entities.Role;
import org.upc.iamservice.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class User extends AuditableAbstractAggregateRoot<User> {

    @Getter
    @NotBlank
    @Column(unique = true, nullable = false, length = 128)
    @Size(max = 128)
    private String auth0Subject;

    @Getter
    @NotBlank
    @Column(unique = true, nullable = false)
    @Size(max = 254)
    private String email;

    @Getter
    @NotBlank
    @Size(max = 60)
    private String firstName;

    @Getter
    @NotBlank
    @Size(max = 60)
    private String lastName;

    @Getter
    @Size(max = 20)
    private String phone;

    @Getter
    private boolean isActive;

    @Getter
    private boolean emailVerified;

    @Getter
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    public User() {
        this.roles = new HashSet<>();
        this.isActive = true;
        this.emailVerified = false;
    }

    public User(String auth0Subject, String email, String firstName, String lastName, String phone) {
        this();
        this.auth0Subject = auth0Subject;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.roles = new HashSet<>();
    }

    public User(String auth0Subject, String email, String firstName, String lastName, String phone, List<Role> roles) {
        this(auth0Subject, email, firstName, lastName, phone);
        addRoles(roles);
    }

    public User addRole(Role role) {
        this.roles.add(role);
        return this;
    }

    public User addRoles(List<Role> roles) {
        var validatedRoleSet = Role.validateRoleSet(roles);
        this.roles.addAll(validatedRoleSet);
        return this;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void deactivateUser() {
        this.isActive = false;
    }

    public void activateUser() {
        this.isActive = true;
    }

    public void updateUserInfo(String email, String firstName, String lastName, String phone) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }
}
