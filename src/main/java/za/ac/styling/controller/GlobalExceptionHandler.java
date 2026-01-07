package za.ac.styling.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        String correlationId = UUID.randomUUID().toString();

        ex.printStackTrace();
        Map<String, Object> body = Map.of(
            "timestamp", Instant.now().toString(),
            "correlationId", correlationId,
            "status", 500,
            "error", "Internal Server Error",
            "message", "An unexpected error occurred",
            "path", ""
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}