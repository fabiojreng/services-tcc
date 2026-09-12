package br.edu.ifma.labmanager.acceptance.scheduling;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cenários de aceitação compartilhados entre scheduling-service-clean e scheduling-service-layered.
 */
public final class ReservationAcceptanceSupport {

    private ReservationAcceptanceSupport() {
    }

    public static ResultActions solicitaReservaValida(MockMvc mockMvc) throws Exception {
        LocalDate nextWednesday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
        if (nextWednesday.isBefore(LocalDate.now().plusDays(2))) {
            nextWednesday = nextWednesday.plusWeeks(1);
        }
        LocalDateTime start = nextWednesday.atTime(10, 0);
        LocalDateTime end = nextWednesday.atTime(12, 0);

        String body = """
                {
                  "laboratoryId": "%s",
                  "requesterId": "prof-demo",
                  "start": "%s",
                  "end": "%s"
                }
                """.formatted(UUID.randomUUID(), start, end);

        return mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    public static void assertReservaCriadaComSucesso(ResultActions result) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.id").exists());
    }
}
