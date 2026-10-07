package br.edu.ifma.labmanager.scheduling.infra;

import br.edu.ifma.labmanager.acceptance.scheduling.ReservationAcceptanceSupport;
import br.edu.ifma.labmanager.scheduling.application.ports.CatalogGateway;
import br.edu.ifma.labmanager.scheduling.application.ports.IdentityGateway;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.OperatingHours;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.time.LocalTime;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SchedulingCleanApplicationTests {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

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
        ReservationAcceptanceSupport.assertReservaCriadaComSucesso(
                ReservationAcceptanceSupport.solicitaReservaValida(mockMvc)
        );
    }
}
