package com.pagoflex.compromisos.dto;

import com.pagoflex.compromisos.model.Compromiso;
import com.pagoflex.compromisos.model.Reporte;
import java.util.List;

public record RegistroReporte(Reporte reporte, Compromiso compromiso, List<MovimientoDto> movimientos) {
}
