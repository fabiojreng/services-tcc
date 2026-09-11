package br.edu.ifma.labmanager.scheduling.domain.policy;

import br.edu.ifma.labmanager.scheduling.domain.entities.Reservation;
import br.edu.ifma.labmanager.scheduling.domain.entities.ReservationFactory;
import br.edu.ifma.labmanager.scheduling.domain.entities.ReservationStatus;
import br.edu.ifma.labmanager.scheduling.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.LaboratoryId;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.OperatingHours;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationPolicyTest {

    private LaboratoryId labId;
    private OperatingHours hours;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        labId = LaboratoryId.of(UUID.randomUUID());
        hours = OperatingHours.weekdays(LocalTime.of(8, 0), LocalTime.of(18, 0));
        // Segunda-feira fixa para previsibilidade
        now = LocalDateTime.of(2026, 9, 7, 10, 0); // segunda
    }

    @Test
    void aceitaReservaValida() {
        TimeSlot slot = TimeSlot.of(
                LocalDateTime.of(2026, 9, 9, 9, 0),  // quarta, +48h
                LocalDateTime.of(2026, 9, 9, 11, 0)
        );

        assertDoesNotThrow(() ->
                ReservationPolicy.validate(slot, hours, now, List.of())
        );
    }

    @Test
    void rejeitaDuracaoMaiorQueQuatroHoras() {
        TimeSlot slot = TimeSlot.of(
                LocalDateTime.of(2026, 9, 9, 8, 0),
                LocalDateTime.of(2026, 9, 9, 13, 0)
        );

        DomainException ex = assertThrows(DomainException.class, () ->
                ReservationPolicy.validate(slot, hours, now, List.of())
        );
        assertTrue(ex.getMessage().contains("4 horas"));
    }

    @Test
    void rejeitaAntecedenciaMenorQue24Horas() {
        TimeSlot slot = TimeSlot.of(
                LocalDateTime.of(2026, 9, 7, 20, 0), // mesmo dia
                LocalDateTime.of(2026, 9, 7, 21, 0)
        );

        DomainException ex = assertThrows(DomainException.class, () ->
                ReservationPolicy.validate(slot, hours, now, List.of())
        );
        assertTrue(ex.getMessage().contains("24 horas"));
    }

    @Test
    void rejeitaForaDoHorarioDeFuncionamento() {
        TimeSlot slot = TimeSlot.of(
                LocalDateTime.of(2026, 9, 9, 19, 0),
                LocalDateTime.of(2026, 9, 9, 20, 0)
        );

        DomainException ex = assertThrows(DomainException.class, () ->
                ReservationPolicy.validate(slot, hours, now, List.of())
        );
        assertTrue(ex.getMessage().contains("horário de funcionamento"));
    }

    @Test
    void rejeitaSobreposicaoComConfirmada() {
        TimeSlot existingSlot = TimeSlot.of(
                LocalDateTime.of(2026, 9, 9, 10, 0),
                LocalDateTime.of(2026, 9, 9, 12, 0)
        );
        Reservation confirmed = Reservation.request(labId, "user-1", existingSlot);
        confirmed.confirm();

        TimeSlot requested = TimeSlot.of(
                LocalDateTime.of(2026, 9, 9, 11, 0),
                LocalDateTime.of(2026, 9, 9, 13, 0)
        );

        DomainException ex = assertThrows(DomainException.class, () ->
                ReservationPolicy.validate(requested, hours, now, List.of(confirmed))
        );
        assertTrue(ex.getMessage().contains("sobreposta"));
    }

    @Test
    void permiteSobreposicaoApenasComSolicitada() {
        TimeSlot existingSlot = TimeSlot.of(
                LocalDateTime.of(2026, 9, 9, 10, 0),
                LocalDateTime.of(2026, 9, 9, 12, 0)
        );
        Reservation requestedOnly = Reservation.request(labId, "user-1", existingSlot);

        TimeSlot requested = TimeSlot.of(
                LocalDateTime.of(2026, 9, 9, 11, 0),
                LocalDateTime.of(2026, 9, 9, 13, 0)
        );

        assertDoesNotThrow(() ->
                ReservationPolicy.validate(requested, hours, now, List.of(requestedOnly))
        );
    }

    @Test
    void factoryCriaComStatusRequested() {
        TimeSlot slot = TimeSlot.of(
                LocalDateTime.of(2026, 9, 9, 9, 0),
                LocalDateTime.of(2026, 9, 9, 11, 0)
        );

        Reservation reservation = ReservationFactory.createRequest(
                labId, "prof-1", slot, hours, now, List.of()
        );

        assertEquals(ReservationStatus.REQUESTED, reservation.status());
        assertEquals(labId, reservation.laboratoryId());
        assertEquals("prof-1", reservation.requesterId());
    }
}
