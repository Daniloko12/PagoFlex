package com.pagoflex.compromisos.dto;

import com.pagoflex.compromisos.model.Anulacion;
import com.pagoflex.compromisos.model.Compromiso;
import java.util.List;

public record RegistroAnulacion(Anulacion anulacion, Compromiso compromiso, List<MovimientoDto> movimientos) {
}
