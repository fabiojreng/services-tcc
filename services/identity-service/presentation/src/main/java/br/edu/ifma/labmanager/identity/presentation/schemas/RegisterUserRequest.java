package br.edu.ifma.labmanager.identity.presentation.schemas;

import br.edu.ifma.labmanager.identity.domain.value_objects.Role;

import java.util.Set;

public record RegisterUserRequest(String userId, String name, Set<Role> roles) {
}
