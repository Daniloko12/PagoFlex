package com.pagoflex.pagos.client;

import com.pagoflex.pagos.dto.ReconocimientoObtenidoDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UsuariosClient {

    private final RestClient cliente;

    public UsuariosClient(RestClient.Builder builder, @Value("${servicios.usuarios.url}") String url) {
        this.cliente = builder.baseUrl(url).build();
    }

    public void registrarReconocimientos(List<ReconocimientoObtenidoDto> reconocimientos) {
        if (reconocimientos == null || reconocimientos.isEmpty()) {
            return;
        }
        cliente.post()
                .uri("/api/reconocimientos/obtenidos")
                .body(reconocimientos)
                .retrieve()
                .toBodilessEntity();
    }
}
