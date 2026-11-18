package com.pagoflex.compromisos.config;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> errorConEstado(ResponseStatusException error) {
        String mensaje = error.getReason() == null ? "No se pudo completar la solicitud" : error.getReason();
        return ResponseEntity.status(error.getStatusCode()).body(Map.of("error", mensaje));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, String>> servicioNoDisponible(RestClientException error) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "No se pudo comunicar con otro microservicio de PagoFlex"));
    }
}
