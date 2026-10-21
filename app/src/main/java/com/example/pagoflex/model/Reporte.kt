package com.example.pagoflex.model

import java.time.LocalDateTime

enum class MotivoReporte(val etiqueta: String) {
    YA_PAGUE("Ya pagué por otro medio"),
    NO_RECONOZCO("No reconozco este cobro"),
    MONTO_INCORRECTO("El monto no corresponde"),
    OTRO("Otro")
}

enum class EstadoReporte(val etiqueta: String) {
    ABIERTO("Abierto"),
    EN_GESTION("En gestión"),
    RESUELTO("Resuelto")
}

data class Reporte(
    val idReporte: Int,
    val numero: String,
    val idCompromiso: Int,
    val motivo: MotivoReporte,
    val comentario: String? = null,
    val fechaCreacion: LocalDateTime,
    val estado: EstadoReporte,
    val idAdministrador: Int? = null,
    val fechaToma: LocalDateTime? = null,
    val respuesta: String? = null,
    val fechaResolucion: LocalDateTime? = null
)
