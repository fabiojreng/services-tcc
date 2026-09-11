package br.edu.ifma.labmanager.scheduling.domain.model;

import br.edu.ifma.labmanager.scheduling.domain.exception.DomainException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Políticas de negócio do Agendamento (Fase 0).
 * <ol>
 *   <li>Sem sobreposição com reservas CONFIRMADAS no mesmo laboratório</li>
 *   <li>Dentro do horário de funcionamento</li>
 *   <li>Antecedência mínima de 24 horas</li>
 *   <li>Duração máxima de 4 horas</li>
 * </ol>
 */
public final class ReservationPolicy {

    public static final Duration MINIMUM_LEAD_TIME = Duration.ofHours(24);
    public static final Duration MAXIMUM_DURATION = Duration.ofHours(4);

    private ReservationPolicy() {
    }

    public static void validate(
            TimeSlot requested,
            OperatingHours operatingHours,
            LocalDateTime now,
            List<Reservation> existingForLaboratory
    ) {
        Objects.requireNonNull(requested);
        Objects.requireNonNull(operatingHours);
        Objects.requireNonNull(now);
        Objects.requireNonNull(existingForLaboratory);

        if (requested.duration().compareTo(MAXIMUM_DURATION) > 0) {
            throw new DomainException("A duração máxima da reserva é de 4 horas");
        }

        if (Duration.between(now, requested.start()).compareTo(MINIMUM_LEAD_TIME) < 0) {
            throw new DomainException("A reserva exige antecedência mínima de 24 horas");
        }

        if (!operatingHours.covers(requested)) {
            throw new DomainException("A reserva está fora do horário de funcionamento do laboratório");
        }

        boolean overlapsConfirmed = existingForLaboratory.stream()
                .filter(r -> r.status() == ReservationStatus.CONFIRMED)
                .anyMatch(r -> r.slot().overlaps(requested));

        if (overlapsConfirmed) {
            throw new DomainException("Já existe reserva confirmada sobreposta neste laboratório");
        }
    }
}
