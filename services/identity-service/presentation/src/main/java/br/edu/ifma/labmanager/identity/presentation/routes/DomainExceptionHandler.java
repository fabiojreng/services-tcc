package br.edu.ifma.labmanager.identity.presentation.routes;

import br.edu.ifma.labmanager.identity.domain.exceptions.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, String>> handle(DomainException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(Map.of("error", ex.getMessage()));
    }
}
