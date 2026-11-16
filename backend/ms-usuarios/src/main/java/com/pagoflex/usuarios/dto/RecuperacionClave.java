package com.pagoflex.usuarios.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecuperacionClave(Integer idUsuario, String codigo, String claveNueva, LocalDateTime ahora, List<MovimientoDto> movimientos) {
}
