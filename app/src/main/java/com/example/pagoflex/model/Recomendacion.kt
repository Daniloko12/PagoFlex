package com.example.pagoflex.model

import java.time.LocalDateTime

enum class EstadoRecomendacion(val etiqueta: String, val orden: Int) {
    RECIBIDA("Recibida", 1),
    CONTACTADA("Contactada", 2),
    EN_EVALUACION("En evaluación", 3),
    SE_UNIO("Se unió", 4),
    DESCARTADA("Descartada", 4)
}

data class Recomendacion(
    val idRecomendacion: Int,
    val numero: String,
    val idUsuarioFinal: Int,
    val nombreOrganizacion: String,
    val rutOrganizacion: String? = null,
    val idRubro: Int,
    val idComuna: Int,
    val fecha: LocalDateTime,
    val estado: EstadoRecomendacion
)

data class ContactoRecomendacion(
    val idRecomendacion: Int,
    val nombre: String,
    val correo: String? = null,
    val telefono: String? = null,
    val autorizaContacto: Boolean
)

data class HistorialRecomendacion(
    val idHistorial: Int,
    val idRecomendacion: Int,
    val estado: EstadoRecomendacion,
    val idAdministrador: Int,
    val fecha: LocalDateTime,
    val motivo: String? = null
)
