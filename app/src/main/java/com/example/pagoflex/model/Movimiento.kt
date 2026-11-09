package com.example.pagoflex.model

import java.time.LocalDateTime

enum class GrupoMovimiento(val etiqueta: String) {
    COMPROMISOS("Compromisos"),
    PAGOS("Pagos"),
    REPORTES("Reportes"),
    CUENTA("Cuenta")
}

enum class TipoMovimiento(val etiqueta: String, val grupo: GrupoMovimiento) {
    COMPROMISO_REGISTRADO("Compromiso registrado", GrupoMovimiento.COMPROMISOS),
    COMPROMISO_MODIFICADO("Compromiso modificado", GrupoMovimiento.COMPROMISOS),
    COMPROMISO_ANULADO("Compromiso anulado", GrupoMovimiento.COMPROMISOS),
    COMPROMISO_VENCIDO("Compromiso vencido", GrupoMovimiento.COMPROMISOS),
    CUOTA_GENERADA("Siguiente cuota generada", GrupoMovimiento.COMPROMISOS),
    PAGO_REGISTRADO("Pago registrado", GrupoMovimiento.PAGOS),
    REPORTE_ENVIADO("Reporte enviado", GrupoMovimiento.REPORTES),
    REPORTE_EN_GESTION("Reporte en gestión", GrupoMovimiento.REPORTES),
    REPORTE_RESUELTO("Reporte resuelto", GrupoMovimiento.REPORTES),
    CUENTA_CREADA("Cuenta creada", GrupoMovimiento.CUENTA),
    DATOS_ACTUALIZADOS("Datos personales actualizados", GrupoMovimiento.CUENTA),
    CLAVE_CAMBIADA("Contraseña cambiada", GrupoMovimiento.CUENTA),
    CLAVE_RECUPERADA("Contraseña recuperada con código", GrupoMovimiento.CUENTA),
    CLAVE_RESTABLECIDA("Contraseña restablecida por un administrador", GrupoMovimiento.CUENTA)
}

data class Movimiento(
    val idMovimiento: Int,
    val tipo: TipoMovimiento,
    val fecha: LocalDateTime,
    val idUsuario: Int,
    val idAutor: Int?,
    val idCompromiso: Int?,
    val detalle: String
)

private fun descripcionCompromiso(c: Compromiso): String {
    val cuota = textoCuota(c)?.let { " ($it)" } ?: ""
    return "\"${c.concepto}\"$cuota por ${formatoPesos(c.monto)}, vence el ${formatoFecha(c.fechaVencimiento)}"
}

fun movimientoRegistro(c: Compromiso, idAdmin: Int?, fecha: LocalDateTime) =
    Movimiento(0, TipoMovimiento.COMPROMISO_REGISTRADO, fecha, c.idUsuarioFinal, idAdmin, c.idCompromiso, descripcionCompromiso(c))

fun movimientoCuotaGenerada(siguiente: Compromiso, fecha: LocalDateTime) =
    Movimiento(0, TipoMovimiento.CUOTA_GENERADA, fecha, siguiente.idUsuarioFinal, null, siguiente.idCompromiso, descripcionCompromiso(siguiente))

fun cambiosEntre(antes: Compromiso, despues: Compromiso): List<String> = listOfNotNull(
    if (antes.concepto != despues.concepto) "Concepto: \"${antes.concepto}\" → \"${despues.concepto}\"" else null,
    if (antes.monto != despues.monto) "Monto: ${formatoPesos(antes.monto)} → ${formatoPesos(despues.monto)}" else null,
    if (antes.fechaVencimiento != despues.fechaVencimiento)
        "Vencimiento: ${formatoFecha(antes.fechaVencimiento)} → ${formatoFecha(despues.fechaVencimiento)}" else null,
    if (antes.numeroCuota != despues.numeroCuota || antes.totalCuotas != despues.totalCuotas)
        "Cuotas: ${textoCuota(antes) ?: "sin cuotas"} → ${textoCuota(despues) ?: "sin cuotas"}" else null,
    if (antes.estado != despues.estado) "Estado: ${antes.estado.etiqueta} → ${despues.estado.etiqueta}" else null
)

fun movimientoModificacion(antes: Compromiso, despues: Compromiso, idAdmin: Int, fecha: LocalDateTime) =
    Movimiento(
        0, TipoMovimiento.COMPROMISO_MODIFICADO, fecha, despues.idUsuarioFinal, idAdmin, despues.idCompromiso,
        cambiosEntre(antes, despues).joinToString(" · ")
    )

fun movimientoAnulacion(a: Anulacion, c: Compromiso) =
    Movimiento(
        0, TipoMovimiento.COMPROMISO_ANULADO, a.fecha, c.idUsuarioFinal, a.idAdministrador, c.idCompromiso,
        "\"${c.concepto}\" · Motivo: ${a.motivo.etiqueta}" + (a.comentario?.let { " · $it" } ?: "")
    )

fun movimientoVencimiento(c: Compromiso) =
    Movimiento(
        0, TipoMovimiento.COMPROMISO_VENCIDO, c.fechaCambioEstado, c.idUsuarioFinal, null, c.idCompromiso,
        "\"${c.concepto}\" venció sin pago" + if (c.recargoAplicado > 0) ". Recargo de ${formatoPesos(c.recargoAplicado)}" else ""
    )

fun movimientoPago(p: Pago, c: Compromiso) =
    Movimiento(
        0, TipoMovimiento.PAGO_REGISTRADO, p.fechaHora, c.idUsuarioFinal, c.idUsuarioFinal, c.idCompromiso,
        "${formatoPesos(p.montoPagado)} por \"${c.concepto}\" con ${p.medioPago.etiqueta.lowercase()} (${p.canal.etiqueta}) · ${p.numeroComprobante}"
    )

fun movimientoReporte(tipo: TipoMovimiento, r: Reporte, c: Compromiso, idAutor: Int?, fecha: LocalDateTime) =
    Movimiento(
        0, tipo, fecha, c.idUsuarioFinal, idAutor, c.idCompromiso,
        when (tipo) {
            TipoMovimiento.REPORTE_RESUELTO -> "${r.numero} sobre \"${c.concepto}\": ${r.respuesta ?: ""}"
            else -> "${r.numero} sobre \"${c.concepto}\" · ${r.motivo.etiqueta}"
        }
    )

fun movimientoCuenta(tipo: TipoMovimiento, idUsuario: Int, idAutor: Int?, fecha: LocalDateTime, detalle: String) =
    Movimiento(0, tipo, fecha, idUsuario, idAutor, null, detalle)

fun movimientosIniciales(
    compromisos: List<Compromiso>,
    pagos: List<Pago>,
    anulaciones: List<Anulacion>,
    reportes: List<Reporte>
): List<Movimiento> {
    val porId = compromisos.associateBy { it.idCompromiso }
    val registros = compromisos.map { movimientoRegistro(it, it.idAdminRegistro, it.fechaRegistro) }
    val vencidos = compromisos.filter { it.estado == EstadoCompromiso.VENCIDO }.map { movimientoVencimiento(it) }
    val pagados = pagos.mapNotNull { p -> porId[p.idCompromiso]?.let { movimientoPago(p, it) } }
    val anulados = anulaciones.mapNotNull { a -> porId[a.idCompromiso]?.let { movimientoAnulacion(a, it) } }
    val deReportes = reportes.flatMap { r ->
        val c = porId[r.idCompromiso] ?: return@flatMap emptyList()
        listOfNotNull(
            movimientoReporte(TipoMovimiento.REPORTE_ENVIADO, r, c, c.idUsuarioFinal, r.fechaCreacion),
            r.fechaToma?.let { movimientoReporte(TipoMovimiento.REPORTE_EN_GESTION, r, c, r.idAdministrador, it) },
            r.fechaResolucion?.let { movimientoReporte(TipoMovimiento.REPORTE_RESUELTO, r, c, r.idAdministrador, it) }
        )
    }
    return (registros + vencidos + pagados + anulados + deReportes)
        .sortedBy { it.fecha }
        .mapIndexed { indice, movimiento -> movimiento.copy(idMovimiento = indice + 1) }
}

fun cambiosDeUsuario(antes: Usuario, despues: Usuario): String = listOfNotNull(
    if (antes.nombres != despues.nombres || antes.apellidos != despues.apellidos) "Nombre: ${nombreCompleto(despues)}" else null,
    if (!antes.correo.equals(despues.correo, ignoreCase = true)) "Correo: ${antes.correo} → ${despues.correo}" else null,
    if (antes.telefono != despues.telefono) "Teléfono actualizado" else null,
    if (antes.idComuna != despues.idComuna) "Comuna actualizada" else null,
    if (antes.activo != despues.activo) (if (despues.activo) "Cuenta activada" else "Cuenta desactivada") else null
).joinToString(" · ").ifEmpty { "Sin cambios visibles" }
