package com.pagoflex.pagos.dto;

import com.pagoflex.pagos.model.Pago;
import com.pagoflex.pagos.model.Tarjeta;
import java.util.List;

public record RegistroPago(
        Pago pago,
        Tarjeta tarjetaNueva,
        CompromisoDto compromiso,
        CompromisoDto siguienteCuota,
        List<ReconocimientoObtenidoDto> reconocimientos,
        List<MovimientoDto> movimientos
) {
}
