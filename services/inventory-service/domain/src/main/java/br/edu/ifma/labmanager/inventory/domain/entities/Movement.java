package br.edu.ifma.labmanager.inventory.domain.entities;

import br.edu.ifma.labmanager.inventory.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.inventory.domain.value_objects.ItemId;
import br.edu.ifma.labmanager.inventory.domain.value_objects.MovementType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Movement {

    private final UUID id;
    private final ItemId itemId;
    private final MovementType type;
    private final int quantity;
    private final String performedBy;
    private final LocalDateTime occurredAt;

    private Movement(
            UUID id,
            ItemId itemId,
            MovementType type,
            int quantity,
            String performedBy,
            LocalDateTime occurredAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.itemId = Objects.requireNonNull(itemId);
        this.type = Objects.requireNonNull(type);
        if (quantity <= 0) {
            throw new DomainException("Quantidade deve ser positiva");
        }
        this.quantity = quantity;
        this.performedBy = Objects.requireNonNull(performedBy);
        if (performedBy.isBlank()) {
            throw new DomainException("Responsável pela movimentação é obrigatório");
        }
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    public static Movement create(
            ItemId itemId,
            MovementType type,
            int quantity,
            String performedBy,
            LocalDateTime occurredAt
    ) {
        return new Movement(UUID.randomUUID(), itemId, type, quantity, performedBy, occurredAt);
    }

    public static Movement restore(
            UUID id,
            ItemId itemId,
            MovementType type,
            int quantity,
            String performedBy,
            LocalDateTime occurredAt
    ) {
        return new Movement(id, itemId, type, quantity, performedBy, occurredAt);
    }

    public UUID id() {
        return id;
    }

    public ItemId itemId() {
        return itemId;
    }

    public MovementType type() {
        return type;
    }

    public int quantity() {
        return quantity;
    }

    public String performedBy() {
        return performedBy;
    }

    public LocalDateTime occurredAt() {
        return occurredAt;
    }
}
