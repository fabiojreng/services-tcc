package br.edu.ifma.labmanager.identity.domain.policy;

import br.edu.ifma.labmanager.identity.domain.entities.User;
import br.edu.ifma.labmanager.identity.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.identity.domain.value_objects.Permission;

public final class AuthorizationPolicy {

    private AuthorizationPolicy() {
    }

    public static boolean allows(User user, Permission permission) {
        return user.hasPermission(permission);
    }

    public static void assertAllows(User user, Permission permission) {
        if (!allows(user, permission)) {
            throw new DomainException(
                    "Usuário sem permissão " + permission.name()
            );
        }
    }
}
