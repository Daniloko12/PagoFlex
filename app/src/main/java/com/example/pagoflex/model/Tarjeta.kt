package com.example.pagoflex.model

import java.time.LocalDateTime
import java.time.YearMonth

enum class MarcaTarjeta(val etiqueta: String) {
    VISA("Visa"),
    MASTERCARD("Mastercard"),
    AMEX("American Express"),
    OTRA("Tarjeta")
}

data class Tarjeta(
    val idTarjeta: Int,
    val idUsuario: Int,
    val medioPago: MedioPago,
    val marca: MarcaTarjeta,
    val ultimos4: String,
    val titular: String,
    val vencimiento: YearMonth,
    val guardada: Boolean = true,
    val fechaRegistro: LocalDateTime
)

data class TarjetaNueva(
    val medioPago: MedioPago,
    val marca: MarcaTarjeta,
    val ultimos4: String,
    val titular: String,
    val vencimiento: YearMonth
)
