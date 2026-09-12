package br.edu.ifma.labmanager.scheduling.infra;

import br.edu.ifma.labmanager.scheduling.application.ports.CatalogGateway;
import br.edu.ifma.labmanager.scheduling.application.ports.IdentityGateway;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.OperatingHours;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @TestConfiguration
    static class Stubs {
        @Bean
        @Primary
        IdentityGateway identityGatewayStub() {
            return (userId, permission) -> true;
        }

        @Bean
        @Primary
        CatalogGateway catalogGatewayStub() {
            return laboratoryId -> OperatingHours.weekdays(LocalTime.of(8, 0), LocalTime.of(18, 0));
        }
    }

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
                  "end": "%s"
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
