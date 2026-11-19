package com.example.pagoflex.model

import java.time.LocalDate
import java.time.YearMonth

enum class FiltroEstado(val etiqueta: String, val estados: Set<EstadoCompromiso>) {
    POR_PAGAR("Por pagar", setOf(EstadoCompromiso.PENDIENTE, EstadoCompromiso.VENCIDO, EstadoCompromiso.EN_REVISION)),
    VENCIDOS("Vencidos", setOf(EstadoCompromiso.VENCIDO)),
    PENDIENTES("Pendientes", setOf(EstadoCompromiso.PENDIENTE)),
    EN_REVISION("En revisión", setOf(EstadoCompromiso.EN_REVISION)),
    PAGADOS("Pagados", setOf(EstadoCompromiso.PAGADO)),
    TODOS("Todos", EstadoCompromiso.entries.toSet())
}

enum class FiltroVencimiento(val etiqueta: String) {
    CUALQUIERA("Cualquier fecha"),
    YA_VENCIO("Ya venció"),
    PROXIMOS_7_DIAS("Próximos 7 días"),
    ESTE_MES("Este mes"),
    PROXIMO_MES("Próximo mes")
}

data class FiltrosCompromiso(
    val estado: FiltroEstado = FiltroEstado.POR_PAGAR,
    val idEmpresa: Int? = null,
    val vencimiento: FiltroVencimiento = FiltroVencimiento.CUALQUIERA
) {
    val cantidadActivos: Int
        get() = listOf(estado != FiltroEstado.POR_PAGAR, idEmpresa != null, vencimiento != FiltroVencimiento.CUALQUIERA).count { it }
}

fun cumpleVencimiento(compromiso: Compromiso, filtro: FiltroVencimiento, hoy: LocalDate): Boolean {
    val fecha = compromiso.fechaVencimiento
    return when (filtro) {
        FiltroVencimiento.CUALQUIERA -> true
        FiltroVencimiento.YA_VENCIO -> fecha.isBefore(hoy)
        FiltroVencimiento.PROXIMOS_7_DIAS -> !fecha.isBefore(hoy) && !fecha.isAfter(hoy.plusDays(7))
        FiltroVencimiento.ESTE_MES -> YearMonth.from(fecha) == YearMonth.from(hoy)
        FiltroVencimiento.PROXIMO_MES -> YearMonth.from(fecha) == YearMonth.from(hoy).plusMonths(1)
    }
}

fun BaseDatos.compromisosFiltrados(idUsuario: Int, filtros: FiltrosCompromiso): List<Compromiso> =
    compromisosDe(idUsuario).filter { c ->
        c.estado in filtros.estado.estados &&
            (filtros.idEmpresa == null || c.idEmpresa == filtros.idEmpresa) &&
            cumpleVencimiento(c, filtros.vencimiento, fechaHoy)
    }
