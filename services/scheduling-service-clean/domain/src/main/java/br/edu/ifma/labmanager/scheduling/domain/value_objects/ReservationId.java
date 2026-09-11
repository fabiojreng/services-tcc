package br.edu.ifma.labmanager.scheduling.domain.value_objects;

import java.util.Objects;
import java.util.UUID;

public final class ReservationId {

    private final UUID value;

    private ReservationId(UUID value) {
        this.value = Objects.requireNonNull(value, "ReservationId não pode ser nulo");
    }

    public static ReservationId generate() {
        return new ReservationId(UUID.randomUUID());
    }

    public static ReservationId of(UUID value) {
        return new ReservationId(value);
    }

    public static ReservationId of(String value) {
        return new ReservationId(UUID.fromString(value));
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReservationId that)) {
            return false;
        }
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
