package br.edu.ifma.labmanager.scheduling.adapter.web;

import br.edu.ifma.labmanager.scheduling.domain.model.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        ReservationStatus status,
        LocalDateTime start,
        LocalDateTime end
) {
}
