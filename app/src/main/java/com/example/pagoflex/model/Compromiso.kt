package com.example.pagoflex.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class EstadoCompromiso(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    VENCIDO("Vencido"),
    PAGADO("Pagado"),
    EN_REVISION("En revisión"),
    ANULADO("Anulado")
}

enum class MotivoAnulacion(val etiqueta: String) {
    REPACTACION("Repactación"),
    ERROR_REGISTRO("Error de registro"),
    PAGADO_EMPRESA("Pagado directamente a la empresa"),
    OTRO("Otro")
}

enum class Canal(val etiqueta: String) {
    APP("App"),
    PORTAL("Portal de pagos"),
    AGENTE_CONVERSACIONAL("Agente conversacional")
}

enum class MedioPago(val etiqueta: String) {
    DEBITO("Tarjeta de débito"),
    CREDITO("Tarjeta de crédito"),
    TRANSFERENCIA("Transferencia"),
    SIMULADO("Pago simulado")
}

data class Compromiso(
    val idCompromiso: Int,
    val folio: String,
    val idUsuarioFinal: Int,
    val idEmpresa: Int,
    val concepto: String,
    val numeroCuota: Int? = null,
    val totalCuotas: Int? = null,
    val monto: Int,
    val fechaVencimiento: LocalDate,
    val estado: EstadoCompromiso,
    val recargoAplicado: Int = 0,
    val fechaCambioEstado: LocalDateTime,
    val idAdminRegistro: Int? = null,
    val fechaRegistro: LocalDateTime
)

data class Anulacion(
    val idCompromiso: Int,
    val idAdministrador: Int,
    val motivo: MotivoAnulacion,
    val comentario: String? = null,
    val fecha: LocalDateTime
)

data class Pago(
    val idPago: Int,
    val numeroComprobante: String,
    val idCompromiso: Int,
    val fechaHora: LocalDateTime,
    val montoPagado: Int,
    val canal: Canal,
    val medioPago: MedioPago,
    val idTarjeta: Int? = null
)
