package br.edu.ifma.labmanager.scheduling.infrastructure.config;

import br.edu.ifma.labmanager.scheduling.adapter.persistence.JpaReservationRepository;
import br.edu.ifma.labmanager.scheduling.adapter.persistence.SpringDataReservationRepository;
import br.edu.ifma.labmanager.scheduling.adapter.time.SystemClockAdapter;
import br.edu.ifma.labmanager.scheduling.application.port.out.ClockPort;
import br.edu.ifma.labmanager.scheduling.application.port.out.ReservationRepository;
import br.edu.ifma.labmanager.scheduling.application.usecase.RequestReservationUseCase;
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
