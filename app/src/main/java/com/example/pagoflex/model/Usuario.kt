package com.example.pagoflex.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class Rol(val etiqueta: String, val descripcion: String) {
    USUARIO_FINAL("Usuario", "Revisa y paga tus compromisos con las empresas que cobran a través de PagoFlex."),
    ADMINISTRADOR("Administrador", "Gestiona clientes y compromisos de todas las empresas, atiende reportes y revisa la cobranza.")
}

data class Comuna(
    val idComuna: Int,
    val nombre: String
)

data class Usuario(
    val idUsuario: Int,
    val rol: Rol,
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val telefono: String? = null,
    val activo: Boolean = true,
    val fechaCreacion: LocalDateTime,
    val idComuna: Int? = null,
    val fechaIncorporacion: LocalDate? = null,
    val avisosActivos: Boolean? = null,
    val diasAnticipacion: Int? = null
)
