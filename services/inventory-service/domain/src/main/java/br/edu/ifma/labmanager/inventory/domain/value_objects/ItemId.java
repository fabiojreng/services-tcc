package br.edu.ifma.labmanager.inventory.domain.value_objects;

import java.util.Objects;
import java.util.UUID;

public record ItemId(UUID value) {

    public ItemId {
        Objects.requireNonNull(value);
    }

    public static ItemId generate() {
        return new ItemId(UUID.randomUUID());
    }

    public static ItemId of(UUID value) {
        return new ItemId(value);
    }
}
