package com.example.pagoflex.model

import java.time.LocalDateTime
import java.time.YearMonth

enum class DiasAnticipacion(val etiqueta: String, val dias: Int) {
    UNO("1 día antes", 1),
    TRES("3 días antes", 3),
    CINCO("5 días antes", 5)
}

data class ResumenSituacion(
    val totalPorPagar: Int,
    val proximoVencimiento: Compromiso?,
    val cantidadVencidos: Int,
    val cantidadEnRevision: Int
)

data class MovimientoHistorial(
    val fecha: LocalDateTime,
    val compromiso: Compromiso,
    val pago: Pago?,
    val anulacion: Anulacion?
)

data class AvisoProgramado(
    val compromiso: Compromiso,
    val fechaEntrega: LocalDateTime,
    val mensaje: String
)

data class ResumenCobranzaMes(
    val idEmpresa: Int,
    val mes: YearMonth,
    val total: Int,
    val anulados: Int,
    val pagados: Int,
    val vencidos: Int,
    val montoRecaudado: Long
)

data class ResumenCobranza(
    val mes: YearMonth,
    val total: Int,
    val anulados: Int,
    val pagados: Int,
    val pendientes: Int,
    val vencidos: Int,
    val enRevision: Int,
    val montoRecaudado: Long,
    val tasa: Double?,
    val esCifraAgregada: Boolean
)
