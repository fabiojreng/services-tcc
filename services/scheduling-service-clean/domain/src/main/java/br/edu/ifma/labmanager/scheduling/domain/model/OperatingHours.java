package br.edu.ifma.labmanager.scheduling.domain.model;

import br.edu.ifma.labmanager.scheduling.domain.exception.DomainException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Horário de funcionamento do laboratório.
 * Na Fase 0 é informado no comando; na Fase 1 virá do catalog-service.
 * Fase 0: reservas devem caber em um único dia civil.
 */
public final class OperatingHours {

    private final LocalTime opensAt;
    private final LocalTime closesAt;
    private final Set<DayOfWeek> openDays;

    private OperatingHours(LocalTime opensAt, LocalTime closesAt, Set<DayOfWeek> openDays) {
        this.opensAt = Objects.requireNonNull(opensAt);
        this.closesAt = Objects.requireNonNull(closesAt);
        if (!closesAt.isAfter(opensAt)) {
            throw new DomainException("Horário de fechamento deve ser após a abertura");
        }
        if (openDays == null || openDays.isEmpty()) {
            throw new DomainException("Pelo menos um dia de funcionamento é obrigatório");
        }
        this.openDays = EnumSet.copyOf(openDays);
    }

    public static OperatingHours of(LocalTime opensAt, LocalTime closesAt, Set<DayOfWeek> openDays) {
        return new OperatingHours(opensAt, closesAt, openDays);
    }

    public static OperatingHours weekdays(LocalTime opensAt, LocalTime closesAt) {
        return of(opensAt, closesAt, EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
    }

    public boolean covers(TimeSlot slot) {
        if (!slot.start().toLocalDate().equals(slot.end().toLocalDate())) {
            return false;
        }
        DayOfWeek day = slot.start().getDayOfWeek();
        if (!openDays.contains(day)) {
            return false;
        }
        LocalTime startTime = slot.start().toLocalTime();
        LocalTime endTime = slot.end().toLocalTime();
        return !startTime.isBefore(opensAt) && !endTime.isAfter(closesAt);
    }

    public LocalTime opensAt() {
        return opensAt;
    }

    public LocalTime closesAt() {
        return closesAt;
    }

    public Set<DayOfWeek> openDays() {
        return EnumSet.copyOf(openDays);
    }
}
