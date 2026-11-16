package com.pagoflex.compromisos.dto;

import com.pagoflex.compromisos.model.Compromiso;
import java.util.List;

public record RegistroCompromiso(Compromiso compromiso, byte[] boleta, List<MovimientoDto> movimientos) {
}
