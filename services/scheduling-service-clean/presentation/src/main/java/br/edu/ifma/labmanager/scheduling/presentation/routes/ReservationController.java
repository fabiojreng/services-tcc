package br.edu.ifma.labmanager.scheduling.presentation.routes;

import br.edu.ifma.labmanager.scheduling.application.use_cases.RequestReservationCommand;
import br.edu.ifma.labmanager.scheduling.application.use_cases.RequestReservationResult;
import br.edu.ifma.labmanager.scheduling.application.use_cases.RequestReservationUseCase;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.LaboratoryId;
import br.edu.ifma.labmanager.scheduling.domain.value_objects.OperatingHours;
import br.edu.ifma.labmanager.scheduling.presentation.schemas.RequestReservationRequest;
import br.edu.ifma.labmanager.scheduling.presentation.schemas.ReservationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final RequestReservationUseCase requestReservationUseCase;

    public ReservationController(RequestReservationUseCase requestReservationUseCase) {
        this.requestReservationUseCase = requestReservationUseCase;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> request(@RequestBody RequestReservationRequest body) {
        OperatingHours hours = OperatingHours.of(body.opensAt(), body.closesAt(), body.openDays());

        RequestReservationCommand command = new RequestReservationCommand(
                LaboratoryId.of(body.laboratoryId()),
                body.requesterId(),
                body.start(),
                body.end(),
                hours
        );

        RequestReservationResult result = requestReservationUseCase.execute(command);

        ReservationResponse response = new ReservationResponse(
                result.id().value(),
                result.status(),
                result.start(),
                result.end()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
