package com.pagoflex.compromisos.dto;

import com.pagoflex.compromisos.model.Compromiso;
import java.util.List;

public record ActualizacionCompromisos(List<Compromiso> compromisos, List<MovimientoDto> movimientos) {
}
