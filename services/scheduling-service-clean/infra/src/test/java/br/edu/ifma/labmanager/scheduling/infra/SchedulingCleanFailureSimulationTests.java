package br.edu.ifma.labmanager.scheduling.infra;

import br.edu.ifma.labmanager.acceptance.scheduling.ReservationAcceptanceSupport;
import br.edu.ifma.labmanager.scheduling.application.RemoteDependencyException;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2 — simula indisponibilidade de Identity/Catalog sem Toxiproxy (equivalente lógico).
 * Evidencia que a falha síncrona degrada o caso de uso independentemente da Clean Architecture.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SchedulingCleanFailureSimulationTests {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class FailingStubs {
        @Bean
        @Primary
        IdentityGateway identityGatewayDown() {
            return (userId, permission) -> {
                throw new RemoteDependencyException("Falha ao consultar identity-service: connection refused");
            };
        }

        @Bean
        @Primary
        CatalogGateway catalogGatewayStub() {
            return laboratoryId -> OperatingHours.weekdays(LocalTime.of(8, 0), LocalTime.of(18, 0));
        }
    }

    @Test
    void reservaFalhaCom503QuandoIdentityIndisponivel() throws Exception {
        ReservationAcceptanceSupport.solicitaReservaValida(mockMvc)
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("identity-service")));
    }
}
