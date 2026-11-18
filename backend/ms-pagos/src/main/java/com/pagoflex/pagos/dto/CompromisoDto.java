package com.pagoflex.pagos.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CompromisoDto(
        Integer idCompromiso,
        String folio,
        Integer idUsuarioFinal,
        Integer idEmpresa,
        String concepto,
        Integer numeroCuota,
        Integer totalCuotas,
        Integer monto,
        LocalDate fechaVencimiento,
        String estado,
        Integer recargoAplicado,
        LocalDateTime fechaCambioEstado,
        Integer idAdminRegistro,
        LocalDateTime fechaRegistro
) {

    public CompromisoDto pagado(LocalDateTime fecha) {
        return new CompromisoDto(idCompromiso, folio, idUsuarioFinal, idEmpresa, concepto, numeroCuota, totalCuotas,
                monto, fechaVencimiento, "PAGADO", recargoAplicado, fecha, idAdminRegistro, fechaRegistro);
    }
}
