package br.edu.ifma.labmanager.scheduling.infra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SchedulingCleanApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void solicitaReservaComSucesso() throws Exception {
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
                  "end": "%s",
                  "opensAt": "08:00:00",
                  "closesAt": "18:00:00",
                  "openDays": ["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"]
                }
                """.formatted(UUID.randomUUID(), start, end);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.id").exists());
    }
}
