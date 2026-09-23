package br.edu.ifma.labmanager.scheduling.infra;

import br.edu.ifma.labmanager.acceptance.scheduling.ReservationAcceptanceSupport;
import br.edu.ifma.labmanager.scheduling.application.RemoteDependencyException;
import br.edu.ifma.labmanager.scheduling.application.ports.CatalogGateway;
import br.edu.ifma.labmanager.scheduling.application.ports.IdentityGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SchedulingCleanCatalogFailureSimulationTests {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class FailingStubs {
        @Bean
        @Primary
        IdentityGateway identityGatewayStub() {
            return (userId, permission) -> true;
        }

        @Bean
        @Primary
        CatalogGateway catalogGatewayDown() {
            return laboratoryId -> {
                throw new RemoteDependencyException("Falha ao consultar catalog-service: connection refused");
            };
        }
    }

    @Test
    void reservaFalhaCom503QuandoCatalogIndisponivel() throws Exception {
        ReservationAcceptanceSupport.solicitaReservaValida(mockMvc)
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("catalog-service")));
    }
}
