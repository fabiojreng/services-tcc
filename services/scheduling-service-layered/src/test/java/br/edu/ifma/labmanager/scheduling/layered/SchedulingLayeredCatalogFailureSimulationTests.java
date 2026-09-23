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

@SpringBootTest
@AutoConfigureMockMvc
class SchedulingLayeredCatalogFailureSimulationTests {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class FailingStubs {
        @Bean
        @Primary
        IdentityClient identityClientStub() {
            return new IdentityClient(RestClient.builder(), "http://localhost:8080") {
                @Override
                public boolean hasPermission(String userId, String permission) {
                    return true;
                }
            };
        }

        @Bean
        @Primary
        CatalogClient catalogClientDown() {
            return new CatalogClient(RestClient.builder(), "http://localhost:8082") {
                @Override
                public LaboratoryHours getLaboratory(UUID laboratoryId) {
                    throw new IllegalStateException("Falha ao consultar catalog-service: connection refused");
                }
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
