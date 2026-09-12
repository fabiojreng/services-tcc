package br.edu.ifma.labmanager.identity.domain.value_objects;

import java.util.EnumSet;
import java.util.Set;

public enum Role {
    STUDENT(EnumSet.of(Permission.RESERVATION_REQUEST)),
    PROFESSOR(EnumSet.of(Permission.RESERVATION_REQUEST)),
    TECHNICIAN(EnumSet.of(Permission.RESERVATION_REQUEST, Permission.LABORATORY_MANAGE)),
    ADMIN(EnumSet.allOf(Permission.class));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = EnumSet.copyOf(permissions);
    }

    public Set<Permission> permissions() {
        return EnumSet.copyOf(permissions);
    }

    public boolean grants(Permission permission) {
        return permissions.contains(permission);
    }
}
