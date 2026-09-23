package br.edu.ifma.labmanager.scheduling.infra.config;

import br.edu.ifma.labmanager.scheduling.application.ports.CatalogGateway;
import br.edu.ifma.labmanager.scheduling.application.ports.ClockPort;
import br.edu.ifma.labmanager.scheduling.application.ports.IdentityGateway;
import br.edu.ifma.labmanager.scheduling.application.use_cases.RequestReservationUseCase;
import br.edu.ifma.labmanager.scheduling.domain.repository.ReservationRepository;
import br.edu.ifma.labmanager.scheduling.infra.repositories.MongoReservationRepository;
import br.edu.ifma.labmanager.scheduling.infra.repositories.SpringDataReservationRepository;
import br.edu.ifma.labmanager.scheduling.infra.time.SystemClockAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SchedulingConfig {

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    ClockPort clockPort() {
        return new SystemClockAdapter();
    }

    @Bean
    @ConditionalOnBean(SpringDataReservationRepository.class)
    ReservationRepository reservationRepository(SpringDataReservationRepository springData) {
        return new MongoReservationRepository(springData);
    }

    @Bean
    RequestReservationUseCase requestReservationUseCase(
            ReservationRepository reservationRepository,
            ClockPort clockPort,
            CatalogGateway catalogGateway,
            IdentityGateway identityGateway
    ) {
        return new RequestReservationUseCase(
                reservationRepository,
                clockPort,
                catalogGateway,
                identityGateway
        );
    }
}
