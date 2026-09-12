package br.edu.ifma.labmanager.identity.domain.entities;

import br.edu.ifma.labmanager.identity.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.identity.domain.value_objects.Permission;
import br.edu.ifma.labmanager.identity.domain.value_objects.Role;
import br.edu.ifma.labmanager.identity.domain.value_objects.UserId;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class User {

    private final UserId id;
    private final String name;
    private final Set<Role> roles;

    private User(UserId id, String name, Set<Role> roles) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        if (name.isBlank()) {
            throw new DomainException("Nome do usuário é obrigatório");
        }
        if (roles == null || roles.isEmpty()) {
            throw new DomainException("Usuário deve ter ao menos um papel");
        }
        this.roles = EnumSet.copyOf(roles);
    }

    public static User register(UserId id, String name, Set<Role> roles) {
        return new User(id, name, roles);
    }

    public static User restore(UserId id, String name, Set<Role> roles) {
        return new User(id, name, roles);
    }

    public boolean hasPermission(Permission permission) {
        return roles.stream().anyMatch(role -> role.grants(permission));
    }

    public UserId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Set<Role> roles() {
        return EnumSet.copyOf(roles);
    }
}
