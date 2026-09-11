package br.edu.ifma.labmanager.scheduling.presentation.schemas;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record RequestReservationRequest(
        UUID laboratoryId,
        String requesterId,
        LocalDateTime start,
        LocalDateTime end,
        LocalTime opensAt,
        LocalTime closesAt,
        Set<DayOfWeek> openDays
) {
}
