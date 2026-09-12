package br.edu.ifma.labmanager.inventory.application.use_cases;

import br.edu.ifma.labmanager.inventory.domain.value_objects.MovementType;

import java.util.UUID;

public record RegisterMovementCommand(
        String requesterId,
        UUID itemId,
        MovementType type,
        int quantity
) {
}
