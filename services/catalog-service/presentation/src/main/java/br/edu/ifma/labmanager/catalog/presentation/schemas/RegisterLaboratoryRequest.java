package br.edu.ifma.labmanager.catalog.presentation.schemas;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public record RegisterLaboratoryRequest(
        String requesterId,
        String name,
        int capacity,
        LocalTime opensAt,
        LocalTime closesAt,
        Set<DayOfWeek> openDays
) {
}
