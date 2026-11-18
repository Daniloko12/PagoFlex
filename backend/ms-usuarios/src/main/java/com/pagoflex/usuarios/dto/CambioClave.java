package com.pagoflex.usuarios.dto;

import java.util.List;

public record CambioClave(String claveActual, String claveNueva, List<MovimientoDto> movimientos) {
}
