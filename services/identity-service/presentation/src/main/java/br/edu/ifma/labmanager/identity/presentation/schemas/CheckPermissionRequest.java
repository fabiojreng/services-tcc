package br.edu.ifma.labmanager.identity.presentation.schemas;

import br.edu.ifma.labmanager.identity.domain.value_objects.Permission;

public record CheckPermissionRequest(String userId, Permission permission) {
}
