package br.edu.ifma.labmanager.scheduling.presentation.routes;

import br.edu.ifma.labmanager.scheduling.application.RemoteDependencyException;
import br.edu.ifma.labmanager.scheduling.domain.exceptions.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, String>> handleDomain(DomainException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RemoteDependencyException.class)
    public ResponseEntity<Map<String, String>> handleRemote(RemoteDependencyException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
