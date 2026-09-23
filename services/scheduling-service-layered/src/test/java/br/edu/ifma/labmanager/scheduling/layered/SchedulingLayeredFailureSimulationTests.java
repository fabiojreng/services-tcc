package br.edu.ifma.labmanager.scheduling.layered;

import br.edu.ifma.labmanager.acceptance.scheduling.ReservationAcceptanceSupport;
import br.edu.ifma.labmanager.scheduling.layered.client.CatalogClient;
import br.edu.ifma.labmanager.scheduling.layered.client.IdentityClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2 — simula Identity/Catalog indisponíveis na variante layered.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SchedulingLayeredFailureSimulationTests {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class FailingStubs {
        @Bean
        @Primary
        IdentityClient identityClientDown() {
            return new IdentityClient(RestClient.builder(), "http://localhost:8080") {
                @Override
                public boolean hasPermission(String userId, String permission) {
                    throw new IllegalStateException("Falha ao consultar identity-service: connection refused");
                }
            };
        }

        @Bean
        @Primary
        CatalogClient catalogClientStub() {
            return new CatalogClient(RestClient.builder(), "http://localhost:8082") {
                @Override
                public LaboratoryHours getLaboratory(UUID laboratoryId) {
                    throw new UnsupportedOperationException("não deve ser chamado");
                }
            };
        }
    }

    @Test
    void reservaFalhaCom503QuandoIdentityIndisponivel() throws Exception {
        ReservationAcceptanceSupport.solicitaReservaValida(mockMvc)
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("identity-service")));
    }
}
