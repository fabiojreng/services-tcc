package br.edu.ifma.labmanager.scheduling.layered.web;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequestReservationRequest(
        UUID laboratoryId,
        String requesterId,
        LocalDateTime start,
        LocalDateTime end
) {
}
