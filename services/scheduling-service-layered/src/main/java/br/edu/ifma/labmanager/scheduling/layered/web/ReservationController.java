package br.edu.ifma.labmanager.scheduling.layered.web;

import br.edu.ifma.labmanager.scheduling.layered.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> request(@RequestBody RequestReservationRequest body) {
        ReservationResponse response = reservationService.requestReservation(
                body.laboratoryId(),
                body.requesterId(),
                body.start(),
                body.end()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
