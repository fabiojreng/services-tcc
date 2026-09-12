package br.edu.ifma.labmanager.inventory.presentation.schemas;

import java.util.UUID;

public record ItemResponse(UUID id, String name, String unit) {
}
