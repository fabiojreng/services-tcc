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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
class SchedulingLayeredApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class Stubs {
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
        CatalogClient catalogClientStub() {
            return new CatalogClient(RestClient.builder(), "http://localhost:8082") {
                @Override
                public LaboratoryHours getLaboratory(UUID laboratoryId) {
                    return new LaboratoryHours(
                            LocalTime.of(8, 0),
                            LocalTime.of(18, 0),
                            EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
                    );
                }
            };
        }
    }

    @Test
    void solicitaReservaComSucessoParidadeComClean() throws Exception {
        ReservationAcceptanceSupport.assertReservaCriadaComSucesso(
                ReservationAcceptanceSupport.solicitaReservaValida(mockMvc)
        );
    }
}
