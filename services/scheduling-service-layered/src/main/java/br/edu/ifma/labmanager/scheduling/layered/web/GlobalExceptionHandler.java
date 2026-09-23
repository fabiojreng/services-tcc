package br.edu.ifma.labmanager.scheduling.layered.web;

import br.edu.ifma.labmanager.scheduling.layered.service.ReservationService.ReservationBusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReservationBusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusiness(ReservationBusinessException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleRemote(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", ex.getMessage()));
    }
}
