package br.edu.ifma.labmanager.identity.application.use_cases;

import br.edu.ifma.labmanager.identity.domain.value_objects.Permission;

import java.util.Objects;

public record CheckPermissionCommand(String userId, Permission permission) {
    public CheckPermissionCommand {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(permission, "permission");
    }
}
