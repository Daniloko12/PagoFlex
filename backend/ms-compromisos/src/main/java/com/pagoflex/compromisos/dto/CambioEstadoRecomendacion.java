package com.pagoflex.compromisos.dto;

import com.pagoflex.compromisos.model.HistorialRecomendacion;
import com.pagoflex.compromisos.model.Recomendacion;

public record CambioEstadoRecomendacion(Recomendacion recomendacion, HistorialRecomendacion historial, ReconocimientoObtenidoDto reconocimiento) {
}
