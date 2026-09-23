package br.edu.ifma.labmanager.scheduling.infra;

import br.edu.ifma.labmanager.acceptance.scheduling.ReservationAcceptanceSupport;
import br.edu.ifma.labmanager.scheduling.application.ports.CatalogGateway;
import br.edu.ifma.labmanager.scheduling.application.ports.IdentityGateway;
import br.edu.ifma.labmanager.scheduling.domain.entities.Reservation;
import br.edu.ifma.labmanager.scheduling.domain.repository.ReservationRepository;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.LaboratoryId;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.OperatingHours;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.ReservationId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,"
                + "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,"
                + "org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration"
})
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

        @Bean
        @Primary
        ReservationRepository inMemoryReservationRepository() {
            Map<ReservationId, Reservation> store = new ConcurrentHashMap<>();
            return new ReservationRepository() {
                @Override
                public Reservation save(Reservation reservation) {
                    store.put(reservation.id(), reservation);
                    return reservation;
                }

                @Override
                public Optional<Reservation> findById(ReservationId id) {
                    return Optional.ofNullable(store.get(id));
                }

                @Override
                public List<Reservation> findByLaboratoryId(LaboratoryId laboratoryId) {
                    return store.values().stream()
                            .filter(r -> r.laboratoryId().equals(laboratoryId))
                            .toList();
                }
            };
        }
    }

    @Test
    void solicitaReservaComSucesso() throws Exception {
        ReservationAcceptanceSupport.assertReservaCriadaComSucesso(
                ReservationAcceptanceSupport.solicitaReservaValida(mockMvc)
        );
    }
}
