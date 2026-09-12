package br.edu.ifma.labmanager.inventory.presentation.schemas;

import br.edu.ifma.labmanager.inventory.domain.value_objects.MovementType;

public record RegisterMovementRequest(String requesterId, MovementType type, int quantity) {
}
