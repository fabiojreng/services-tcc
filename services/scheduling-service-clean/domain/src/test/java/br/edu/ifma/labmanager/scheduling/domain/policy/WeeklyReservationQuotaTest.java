package br.edu.ifma.labmanager.scheduling.domain.policy;

import br.edu.ifma.labmanager.scheduling.domain.entities.Reservation;
import br.edu.ifma.labmanager.scheduling.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.LaboratoryId;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeeklyReservationQuotaTest {

    private final LaboratoryId labId = LaboratoryId.of(UUID.randomUUID());

    private Reservation requestedOn(int day) {
        return Reservation.request(labId, "prof-1", TimeSlot.of(
                LocalDateTime.of(2026, 9, day, 9, 0),
                LocalDateTime.of(2026, 9, day, 10, 0)
        ));
    }

    @Test
    void semanaIsoComecaNaSegundaAnterior() {
        TimeSlot domingo = TimeSlot.of(
                LocalDateTime.of(2026, 9, 13, 9, 0),
                LocalDateTime.of(2026, 9, 13, 10, 0)
        );

        assertEquals(LocalDateTime.of(2026, 9, 7, 0, 0), WeeklyReservationQuota.weekStart(domingo));
        assertEquals(LocalDateTime.of(2026, 9, 14, 0, 0), WeeklyReservationQuota.weekEndExclusive(domingo));
    }

    @Test
    void aceitaAteDuasReservasAtivas() {
        assertDoesNotThrow(() -> WeeklyReservationQuota.validate(List.of(requestedOn(8), requestedOn(9))));
    }

    @Test
    void rejeitaQuandoJaExistemTresAtivas() {
        Reservation confirmed = requestedOn(10);
        confirmed.confirm();

        assertThrows(DomainException.class, () ->
                WeeklyReservationQuota.validate(List.of(requestedOn(8), requestedOn(9), confirmed))
        );
    }

    @Test
    void canceladasNaoContam() {
        Reservation cancelled = requestedOn(10);
        cancelled.cancel();

        assertDoesNotThrow(() ->
                WeeklyReservationQuota.validate(List.of(requestedOn(8), requestedOn(9), cancelled))
        );
    }
}
