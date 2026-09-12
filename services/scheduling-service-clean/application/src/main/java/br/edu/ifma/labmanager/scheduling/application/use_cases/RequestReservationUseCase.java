package br.edu.ifma.labmanager.scheduling.application.use_cases;

import br.edu.ifma.labmanager.scheduling.application.ports.CatalogGateway;
import br.edu.ifma.labmanager.scheduling.application.ports.ClockPort;
import br.edu.ifma.labmanager.scheduling.application.ports.IdentityGateway;
import br.edu.ifma.labmanager.scheduling.domain.entities.Reservation;
import br.edu.ifma.labmanager.scheduling.domain.entities.ReservationFactory;
import br.edu.ifma.labmanager.scheduling.domain.exceptions.DomainException;
import br.edu.ifma.labmanager.scheduling.domain.repository.ReservationRepository;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.OperatingHours;

import java.util.List;
import java.util.Objects;

public class RequestReservationUseCase {

    public static final String RESERVATION_REQUEST = "RESERVATION_REQUEST";

    private final ReservationRepository reservationRepository;
    private final ClockPort clock;
    private final CatalogGateway catalogGateway;
    private final IdentityGateway identityGateway;

    public RequestReservationUseCase(
            ReservationRepository reservationRepository,
            ClockPort clock,
            CatalogGateway catalogGateway,
            IdentityGateway identityGateway
    ) {
        this.reservationRepository = Objects.requireNonNull(reservationRepository);
        this.clock = Objects.requireNonNull(clock);
        this.catalogGateway = Objects.requireNonNull(catalogGateway);
        this.identityGateway = Objects.requireNonNull(identityGateway);
    }

    public RequestReservationResult execute(RequestReservationCommand command) {
        if (!identityGateway.hasPermission(command.requesterId(), RESERVATION_REQUEST)) {
            throw new DomainException("Usuário sem permissão RESERVATION_REQUEST");
        }

        OperatingHours operatingHours = catalogGateway.getOperatingHours(command.laboratoryId());
        List<Reservation> existing = reservationRepository.findByLaboratoryId(command.laboratoryId());

        Reservation reservation = ReservationFactory.createRequest(
                command.laboratoryId(),
                command.requesterId(),
                command.toSlot(),
                operatingHours,
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
