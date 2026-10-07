package br.edu.ifma.labmanager.scheduling.domain.policy;

import br.edu.ifma.labmanager.scheduling.domain.entities.Reservation;
import br.edu.ifma.labmanager.scheduling.domain.entities.ReservationStatus;
import br.edu.ifma.labmanager.scheduling.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Cota semanal: no máximo 3 reservas ativas (solicitadas ou confirmadas) por solicitante
 * com início na mesma semana ISO (segunda a domingo) do slot solicitado.
 */
public final class WeeklyReservationQuota {

    public static final int MAXIMUM_PER_WEEK = 3;
    private static final Set<ReservationStatus> ACTIVE = EnumSet.of(ReservationStatus.REQUESTED, ReservationStatus.CONFIRMED);

    private WeeklyReservationQuota() {
    }

    public static LocalDateTime weekStart(TimeSlot requested) {
        return requested.start().toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
    }

    public static LocalDateTime weekEndExclusive(TimeSlot requested) {
        return weekStart(requested).plusWeeks(1);
    }

    public static void validate(List<Reservation> requesterReservationsInWeek) {
        Objects.requireNonNull(requesterReservationsInWeek);
        long active = requesterReservationsInWeek.stream()
                .filter(r -> ACTIVE.contains(r.status()))
                .count();
        if (active >= MAXIMUM_PER_WEEK) {
            throw new DomainException("O solicitante já possui " + MAXIMUM_PER_WEEK + " reservas ativas nesta semana");
        }
    }
}
