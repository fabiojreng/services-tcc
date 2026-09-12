package br.edu.ifma.labmanager.identity.domain.policy;

import br.edu.ifma.labmanager.identity.domain.entities.User;
import br.edu.ifma.labmanager.identity.domain.value_objects.Permission;
import br.edu.ifma.labmanager.identity.domain.value_objects.Role;
import br.edu.ifma.labmanager.identity.domain.value_objects.UserId;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationPolicyTest {

    @Test
    void professorPodeSolicitarReserva() {
        User user = User.register(UserId.of("prof-1"), "Ana", EnumSet.of(Role.PROFESSOR));
        assertTrue(AuthorizationPolicy.allows(user, Permission.RESERVATION_REQUEST));
        assertFalse(AuthorizationPolicy.allows(user, Permission.LABORATORY_MANAGE));
    }

    @Test
    void tecnicoPodeGerenciarLaboratorio() {
        User user = User.register(UserId.of("tec-1"), "Bruno", EnumSet.of(Role.TECHNICIAN));
        assertTrue(AuthorizationPolicy.allows(user, Permission.LABORATORY_MANAGE));
    }
}
