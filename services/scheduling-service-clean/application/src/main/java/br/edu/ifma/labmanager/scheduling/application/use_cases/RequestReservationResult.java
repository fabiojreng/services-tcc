package br.edu.ifma.labmanager.scheduling.application.use_cases;

import br.edu.ifma.labmanager.scheduling.domain.entities.ReservationStatus;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.ReservationId;

import java.time.LocalDateTime;

public record RequestReservationResult(
        ReservationId id,
        ReservationStatus status,
        LocalDateTime start,
        LocalDateTime end
) {
}
