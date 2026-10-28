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

data class AvanceEmpresa(
    val empresa: EmpresaCliente,
    val actual: Compromiso?,
    val porPagar: Int,
    val ultimoPagado: Compromiso?
) {
    val alDia: Boolean get() = actual == null
}

data class CantidadEstado(
    val estado: EstadoCompromiso,
    val cantidad: Int
)

data class RecaudacionMes(
    val mes: YearMonth,
    val monto: Long
)

data class DeudaEmpresa(
    val empresa: EmpresaCliente,
    val monto: Long,
    val cantidad: Int
)

data class ResumenPanel(
    val montoPorCobrar: Long,
    val cantidadPorCobrar: Int,
    val montoVencido: Long,
    val cantidadVencidos: Int,
    val recaudadoMes: Long,
    val pagosMes: Int,
    val clientesActivos: Int,
    val clientesConDeuda: Int,
    val reportesAbiertos: Int,
    val reportesAtrasados: Int,
    val recomendacionesEnCurso: Int,
    val porEstado: List<CantidadEstado>,
    val recaudacionPorMes: List<RecaudacionMes>,
    val deudaPorEmpresa: List<DeudaEmpresa>,
    val vencidosAntiguos: List<Compromiso>,
    val proximosVencimientos: List<Compromiso>
)
