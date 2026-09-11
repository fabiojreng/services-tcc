package br.edu.ifma.labmanager.scheduling.presentation.schemas;

import br.edu.ifma.labmanager.scheduling.domain.entities.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        ReservationStatus status,
        LocalDateTime start,
        LocalDateTime end
) {
}
