package br.edu.ifma.labmanager.scheduling.application.use_cases;

import br.edu.ifma.labmanager.scheduling.domain.value_objects.LaboratoryId;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.OperatingHours;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.TimeSlot;

import java.time.LocalDateTime;
import java.util.Objects;

public record RequestReservationCommand(
        LaboratoryId laboratoryId,
        String requesterId,
        LocalDateTime start,
        LocalDateTime end,
        OperatingHours operatingHours
) {
    public RequestReservationCommand {
        Objects.requireNonNull(laboratoryId, "laboratoryId");
        Objects.requireNonNull(requesterId, "requesterId");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        Objects.requireNonNull(operatingHours, "operatingHours");
    }

    public TimeSlot toSlot() {
        return TimeSlot.of(start, end);
    }
}
