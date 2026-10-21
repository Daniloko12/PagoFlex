package com.example.pagoflex.model

import java.time.LocalDateTime

enum class CategoriaCumplimiento(val etiqueta: String) {
    EJEMPLAR("Ejemplar"),
    AL_DIA("Al día"),
    EN_CAMINO("En camino"),
    INSUFICIENTE("Historial insuficiente")
}

data class Reconocimiento(
    val idReconocimiento: Int,
    val nombre: String,
    val descripcion: String
)

data class UsuarioReconocimiento(
    val idUsuarioFinal: Int,
    val idReconocimiento: Int,
    val fechaObtencion: LocalDateTime,
    val motivo: String
)

data class CumplimientoHistorico(
    val idUsuarioFinal: Int,
    val considerados: Int,
    val aTiempo: Int
)

data class IndicadorCumplimiento(
    val considerados: Int,
    val aTiempo: Int,
    val porcentaje: Double?,
    val categoria: CategoriaCumplimiento
)
