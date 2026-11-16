package com.pagoflex.pagos.client;

import com.pagoflex.pagos.dto.ActualizacionCompromisosDto;
import com.pagoflex.pagos.dto.CompromisoDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CompromisosClient {

    private final RestClient cliente;

    public CompromisosClient(RestClient.Builder builder, @Value("${servicios.compromisos.url}") String url) {
        this.cliente = builder.baseUrl(url).build();
    }

    public CompromisoDto obtener(Integer idCompromiso) {
        try {
            return cliente.get()
                    .uri("/api/compromisos/{id}", idCompromiso)
                    .retrieve()
                    .body(CompromisoDto.class);
        } catch (HttpClientErrorException.NotFound error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe el compromiso " + idCompromiso);
        }
    }

    public void actualizar(List<CompromisoDto> compromisos) {
        if (compromisos.isEmpty()) {
            return;
        }
        cliente.put()
                .uri("/api/compromisos")
                .body(new ActualizacionCompromisosDto(compromisos, List.of()))
                .retrieve()
                .toBodilessEntity();
    }
}
