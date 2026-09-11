package br.edu.ifma.labmanager.scheduling.application.use_cases;

import br.edu.ifma.labmanager.scheduling.application.ports.ClockPort;
import br.edu.ifma.labmanager.scheduling.domain.entities.Reservation;
import br.edu.ifma.labmanager.scheduling.domain.entities.ReservationFactory;
import br.edu.ifma.labmanager.scheduling.domain.repository.ReservationRepository;

import java.util.List;
import java.util.Objects;

public class RequestReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final ClockPort clock;

    public RequestReservationUseCase(ReservationRepository reservationRepository, ClockPort clock) {
        this.reservationRepository = Objects.requireNonNull(reservationRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    public RequestReservationResult execute(RequestReservationCommand command) {
        List<Reservation> existing = reservationRepository.findByLaboratoryId(command.laboratoryId());

        Reservation reservation = ReservationFactory.createRequest(
                command.laboratoryId(),
                command.requesterId(),
                command.toSlot(),
                command.operatingHours(),
                clock.now(),
                existing
        );

        Reservation saved = reservationRepository.save(reservation);

        return new RequestReservationResult(
                saved.id(),
                saved.status(),
                saved.slot().start(),
                saved.slot().end()
        );
    }
}
