package br.edu.ifma.labmanager.scheduling.domain.model;

import br.edu.ifma.labmanager.scheduling.domain.exception.DomainException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Janela temporal de uma reserva.
 * Contém regras estruturais (início &lt; fim); regras de negócio de antecedência/duração
 * ficam em {@link ReservationPolicy}.
 */
public final class TimeSlot {

    private final LocalDateTime start;
    private final LocalDateTime end;

    private TimeSlot(LocalDateTime start, LocalDateTime end) {
        this.start = Objects.requireNonNull(start, "início obrigatório");
        this.end = Objects.requireNonNull(end, "fim obrigatório");
        if (!end.isAfter(start)) {
            throw new DomainException("O fim da reserva deve ser posterior ao início");
        }
    }

    public static TimeSlot of(LocalDateTime start, LocalDateTime end) {
        return new TimeSlot(start, end);
    }

    public LocalDateTime start() {
        return start;
    }

    public LocalDateTime end() {
        return end;
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    public boolean overlaps(TimeSlot other) {
        Objects.requireNonNull(other, "outro slot obrigatório");
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimeSlot timeSlot)) {
            return false;
        }
        return start.equals(timeSlot.start) && end.equals(timeSlot.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
