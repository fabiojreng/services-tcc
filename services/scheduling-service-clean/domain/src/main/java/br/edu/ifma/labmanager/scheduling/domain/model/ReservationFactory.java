package br.edu.ifma.labmanager.scheduling.domain.model;

import java.util.List;

/**
 * Serviço de domínio que orquestra a criação de uma solicitação de reserva
 * aplicando {@link ReservationPolicy}.
 */
public final class ReservationFactory {

    private ReservationFactory() {
    }

    public static Reservation createRequest(
            LaboratoryId laboratoryId,
            String requesterId,
            TimeSlot slot,
            OperatingHours operatingHours,
            java.time.LocalDateTime now,
            List<Reservation> existingForLaboratory
    ) {
        ReservationPolicy.validate(slot, operatingHours, now, existingForLaboratory);
        return Reservation.request(laboratoryId, requesterId, slot);
    }
}
