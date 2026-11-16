package com.pagoflex.pagos.dto;

import java.util.List;

public record ActualizacionCompromisosDto(List<CompromisoDto> compromisos, List<MovimientoDto> movimientos) {
}
