package br.edu.ifma.labmanager.catalog.presentation.schemas;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record LaboratoryResponse(
        UUID id,
        String name,
        int capacity,
        LocalTime opensAt,
        LocalTime closesAt,
        Set<DayOfWeek> openDays
) {
}
