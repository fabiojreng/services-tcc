package br.edu.ifma.labmanager.scheduling.infra.config;

import br.edu.ifma.labmanager.scheduling.application.ports.ClockPort;
import br.edu.ifma.labmanager.scheduling.application.use_cases.RequestReservationUseCase;
import br.edu.ifma.labmanager.scheduling.domain.repository.ReservationRepository;
import br.edu.ifma.labmanager.scheduling.infra.repositories.JpaReservationRepository;
import br.edu.ifma.labmanager.scheduling.infra.repositories.SpringDataReservationRepository;
import br.edu.ifma.labmanager.scheduling.infra.time.SystemClockAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchedulingConfig {

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
            ClockPort clockPort
    ) {
        return new RequestReservationUseCase(reservationRepository, clockPort);
    }
}
