package br.edu.ifma.labmanager.catalog.application.use_cases;

import br.edu.ifma.labmanager.catalog.domain.value_objects.OperatingHours;

import java.util.Objects;

public record RegisterLaboratoryCommand(
        String requesterId,
        String name,
        int capacity,
        OperatingHours operatingHours
) {
    public RegisterLaboratoryCommand {
        Objects.requireNonNull(requesterId, "requesterId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(operatingHours, "operatingHours");
    }
}
