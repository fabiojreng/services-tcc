package br.edu.ifma.labmanager.scheduling.infra.config;

import br.edu.ifma.labmanager.scheduling.application.ports.CatalogGateway;
import br.edu.ifma.labmanager.scheduling.application.ports.ClockPort;
import br.edu.ifma.labmanager.scheduling.application.ports.IdentityGateway;
import br.edu.ifma.labmanager.scheduling.application.use_cases.RequestReservationUseCase;
import br.edu.ifma.labmanager.scheduling.domain.repository.ReservationRepository;
import br.edu.ifma.labmanager.scheduling.infra.repositories.JpaReservationRepository;
import br.edu.ifma.labmanager.scheduling.infra.repositories.SpringDataReservationRepository;
import br.edu.ifma.labmanager.scheduling.infra.time.SystemClockAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class SchedulingConfig {

    @Bean
    RestClient.Builder restClientBuilder() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(2));
        return RestClient.builder().requestFactory(requestFactory);
    }

    @Bean
    ClockPort clockPort() {
        return new SystemClockAdapter();
    }

    @Bean
    ReservationRepository reservationRepository(SpringDataReservationRepository springData) {
        return new JpaReservationRepository(springData);
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
