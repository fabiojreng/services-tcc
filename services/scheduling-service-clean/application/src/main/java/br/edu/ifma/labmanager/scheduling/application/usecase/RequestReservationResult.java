package br.edu.ifma.labmanager.scheduling.application.usecase;

import br.edu.ifma.labmanager.scheduling.domain.model.ReservationId;
import br.edu.ifma.labmanager.scheduling.domain.model.ReservationStatus;

import java.time.LocalDateTime;

public record RequestReservationResult(
        ReservationId id,
        ReservationStatus status,
        LocalDateTime start,
        LocalDateTime end
) {
}
