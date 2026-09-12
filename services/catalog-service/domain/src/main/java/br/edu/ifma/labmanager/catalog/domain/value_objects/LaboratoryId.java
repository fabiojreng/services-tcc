package br.edu.ifma.labmanager.catalog.domain.value_objects;

import java.util.Objects;
import java.util.UUID;

public final class LaboratoryId {
    private final UUID value;

    private LaboratoryId(UUID value) {
        this.value = Objects.requireNonNull(value);
    }

    public static LaboratoryId of(UUID value) {
        return new LaboratoryId(value);
    }

    public static LaboratoryId of(String value) {
        return new LaboratoryId(UUID.fromString(value));
    }

    public static LaboratoryId generate() {
        return new LaboratoryId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LaboratoryId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
