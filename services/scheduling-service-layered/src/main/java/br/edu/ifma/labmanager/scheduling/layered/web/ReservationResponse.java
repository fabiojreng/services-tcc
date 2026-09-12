package br.edu.ifma.labmanager.scheduling.layered.web;

import br.edu.ifma.labmanager.scheduling.layered.entity.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        ReservationStatus status,
        LocalDateTime start,
        LocalDateTime end
) {
}
