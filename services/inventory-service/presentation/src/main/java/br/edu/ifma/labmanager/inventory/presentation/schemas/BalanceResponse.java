package br.edu.ifma.labmanager.inventory.presentation.schemas;

import java.util.UUID;

public record BalanceResponse(UUID itemId, int balance) {
}
