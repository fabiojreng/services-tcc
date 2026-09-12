package br.edu.ifma.labmanager.identity.application.use_cases;

import br.edu.ifma.labmanager.identity.domain.value_objects.Role;

import java.util.Objects;
import java.util.Set;

public record RegisterUserCommand(String userId, String name, Set<Role> roles) {
    public RegisterUserCommand {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(roles, "roles");
    }
}
