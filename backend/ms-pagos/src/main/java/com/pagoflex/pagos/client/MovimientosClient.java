package com.pagoflex.pagos.client;

import com.pagoflex.pagos.dto.MovimientoDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MovimientosClient {

    private final RestClient cliente;

    public MovimientosClient(RestClient.Builder builder, @Value("${servicios.movimientos.url}") String url) {
        this.cliente = builder.baseUrl(url).build();
    }

    public void registrar(List<MovimientoDto> movimientos) {
        if (movimientos == null || movimientos.isEmpty()) {
            return;
        }
        cliente.post()
                .uri("/api/movimientos")
                .body(movimientos)
                .retrieve()
                .toBodilessEntity();
    }
}
