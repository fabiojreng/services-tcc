package br.edu.ifma.labmanager.scheduling.adapter.web;

import br.edu.ifma.labmanager.scheduling.application.usecase.RequestReservationCommand;
import br.edu.ifma.labmanager.scheduling.application.usecase.RequestReservationResult;
import br.edu.ifma.labmanager.scheduling.application.usecase.RequestReservationUseCase;
import br.edu.ifma.labmanager.scheduling.domain.model.LaboratoryId;
import br.edu.ifma.labmanager.scheduling.domain.model.OperatingHours;
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
