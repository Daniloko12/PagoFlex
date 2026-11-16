package com.pagoflex.compromisos.dto;

import java.time.LocalDateTime;

public record ReconocimientoObtenidoDto(
        Integer idUsuarioFinal,
        Integer idReconocimiento,
        LocalDateTime fechaObtencion,
        String motivo
) {
}
