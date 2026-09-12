package br.edu.ifma.labmanager.inventory.presentation.schemas;

import br.edu.ifma.labmanager.inventory.domain.value_objects.MovementType;

import java.time.LocalDateTime;
import java.util.UUID;

public record MovementResponse(
        UUID id,
        UUID itemId,
        MovementType type,
        int quantity,
        String performedBy,
        LocalDateTime occurredAt
) {
}
