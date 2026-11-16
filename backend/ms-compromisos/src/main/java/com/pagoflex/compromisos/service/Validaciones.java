package com.pagoflex.compromisos.service;

import com.pagoflex.compromisos.dto.MovimientoDto;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class Validaciones {

    private Validaciones() {
    }

    static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    static boolean sinId(Integer id) {
        return id == null || id <= 0;
    }

    static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
    }

    static List<MovimientoDto> conCompromiso(List<MovimientoDto> movimientos, Integer idCompromiso) {
        return movimientos == null ? List.of() : movimientos.stream().map(m -> m.conCompromiso(idCompromiso)).toList();
    }
}
