package com.example.pagoflex.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth

data class BaseDatos(
    val usuarios: List<Usuario> = DatosAnexo9.usuarios,
    val empresas: List<EmpresaCliente> = DatosAnexo9.empresas,
    val compromisos: List<Compromiso> =
        actualizarVencimientos(DatosAnexo9.compromisos, DatosAnexo9.empresas, DatosAnexo9.FECHA_HOY),
    val pagos: List<Pago> = DatosAnexo9.pagos,
    val anulaciones: List<Anulacion> = DatosAnexo9.anulaciones,
    val reportes: List<Reporte> = DatosAnexo9.reportes,
    val recomendaciones: List<Recomendacion> = DatosAnexo9.recomendaciones,
    val contactosRecomendacion: List<ContactoRecomendacion> = DatosAnexo9.contactosRecomendacion,
    val historialRecomendaciones: List<HistorialRecomendacion> = DatosAnexo9.historialRecomendaciones,
    val reconocimientosObtenidos: List<UsuarioReconocimiento> = DatosAnexo9.reconocimientosObtenidos,
    val tarjetas: List<Tarjeta> = DatosAnexo9.tarjetas,
    val rubros: List<Rubro> = DatosAnexo9.rubros,
    val comunas: List<Comuna> = DatosAnexo9.comunas,
    val reconocimientos: List<Reconocimiento> = DatosAnexo9.reconocimientos,
    val fechaHoy: LocalDate = DatosAnexo9.FECHA_HOY
)

fun BaseDatos.usuarioPorId(id: Int?): Usuario? = usuarios.find { it.idUsuario == id }

fun BaseDatos.empresaPorId(id: Int?): EmpresaCliente? = empresas.find { it.idEmpresa == id }

fun BaseDatos.compromisoPorId(id: Int?): Compromiso? = compromisos.find { it.idCompromiso == id }

fun BaseDatos.pagoDe(idCompromiso: Int): Pago? = pagos.find { it.idCompromiso == idCompromiso }

fun BaseDatos.pagoPorComprobante(numero: String?): Pago? = pagos.find { it.numeroComprobante == numero }

fun BaseDatos.anulacionDe(idCompromiso: Int): Anulacion? = anulaciones.find { it.idCompromiso == idCompromiso }

fun BaseDatos.reportePorId(id: Int?): Reporte? = reportes.find { it.idReporte == id }

fun BaseDatos.recomendacionPorId(id: Int?): Recomendacion? = recomendaciones.find { it.idRecomendacion == id }

fun BaseDatos.tarjetaPorId(id: Int?): Tarjeta? = tarjetas.find { it.idTarjeta == id }

fun BaseDatos.rubroPorId(id: Int?): Rubro? = rubros.find { it.idRubro == id }

fun BaseDatos.comunaPorId(id: Int?): Comuna? = comunas.find { it.idComuna == id }

fun BaseDatos.reconocimientoPorId(id: Int): Reconocimiento? = reconocimientos.find { it.idReconocimiento == id }

fun BaseDatos.usuariosDelRol(rol: Rol): List<Usuario> = usuarios.filter { it.rol == rol && it.activo }

fun BaseDatos.usuarioPorRut(rut: String): Usuario? {
    val buscado = normalizarRut(rut)
    return usuarios.find { normalizarRut(it.rut) == buscado }
}

fun BaseDatos.usuarioPorCorreo(correo: String): Usuario? =
    usuarios.find { it.correo.equals(correo.trim(), ignoreCase = true) }

fun BaseDatos.reporteActivoDe(idCompromiso: Int): Reporte? =
    reportes.find { it.idCompromiso == idCompromiso && it.estado != EstadoReporte.RESUELTO }

fun BaseDatos.ultimoReporteDe(idCompromiso: Int): Reporte? =
    reportes.filter { it.idCompromiso == idCompromiso }.maxByOrNull { it.fechaCreacion }

fun BaseDatos.compromisosVigentes(idUsuario: Int): List<Compromiso> =
    compromisos
        .filter {
            it.idUsuarioFinal == idUsuario && (it.estado == EstadoCompromiso.PENDIENTE ||
                it.estado == EstadoCompromiso.VENCIDO || it.estado == EstadoCompromiso.EN_REVISION)
        }
        .sortedBy { it.fechaVencimiento }

fun BaseDatos.resumenSituacion(idUsuario: Int): ResumenSituacion {
    val vigentes = compromisosVigentes(idUsuario)
    val mesActual = YearMonth.from(fechaHoy)
    val porPagar = vigentes.filter {
        it.estado == EstadoCompromiso.VENCIDO ||
            (it.estado == EstadoCompromiso.PENDIENTE && !YearMonth.from(it.fechaVencimiento).isAfter(mesActual))
    }
    return ResumenSituacion(
        totalPorPagar = porPagar.sumOf { totalAPagar(it) },
        proximoVencimiento = vigentes
            .filter { it.estado == EstadoCompromiso.PENDIENTE && !it.fechaVencimiento.isBefore(fechaHoy) }
            .minByOrNull { it.fechaVencimiento },
        cantidadVencidos = vigentes.count { it.estado == EstadoCompromiso.VENCIDO },
        cantidadEnRevision = vigentes.count { it.estado == EstadoCompromiso.EN_REVISION }
    )
}

fun BaseDatos.compromisoQueDebePagarseAntes(compromiso: Compromiso): Compromiso? =
    compromisos
        .filter {
            it.idUsuarioFinal == compromiso.idUsuarioFinal &&
                it.idEmpresa == compromiso.idEmpresa &&
                it.idCompromiso != compromiso.idCompromiso &&
                (it.estado == EstadoCompromiso.PENDIENTE || it.estado == EstadoCompromiso.VENCIDO) &&
                it.fechaVencimiento.isBefore(compromiso.fechaVencimiento)
        }
        .minByOrNull { it.fechaVencimiento }

fun BaseDatos.empresasDelUsuario(idUsuario: Int): List<EmpresaCliente> {
    val ids = compromisos.filter { it.idUsuarioFinal == idUsuario }.map { it.idEmpresa }.toSet()
    return empresas.filter { it.idEmpresa in ids }
}

fun BaseDatos.movimientos(idUsuario: Int): List<MovimientoHistorial> {
    val propios = compromisos.filter { it.idUsuarioFinal == idUsuario }
    val conPago = propios.mapNotNull { c -> pagoDe(c.idCompromiso)?.let { p -> MovimientoHistorial(p.fechaHora, c, p, null) } }
    val anulados = propios.mapNotNull { c -> anulacionDe(c.idCompromiso)?.let { a -> MovimientoHistorial(a.fecha, c, null, a) } }
    return (conPago + anulados).sortedByDescending { it.fecha }
}

fun BaseDatos.aniosHistorial(idUsuario: Int): List<Int> =
    movimientos(idUsuario).map { it.fecha.year }.distinct().sortedDescending()

fun BaseDatos.historialFiltrado(idUsuario: Int, idEmpresa: Int?, anio: Int?, mes: Int?): List<MovimientoHistorial> =
    movimientos(idUsuario).filter { m ->
        (idEmpresa == null || m.compromiso.idEmpresa == idEmpresa) &&
            (anio == null || m.fecha.year == anio) &&
            (mes == null || m.fecha.monthValue == mes)
    }

fun indicadorDe(idUsuario: Int): IndicadorCumplimiento {
    val base = DatosAnexo9.cumplimientoHistorico.find { it.idUsuarioFinal == idUsuario }
    val considerados = base?.considerados ?: 0
    val aTiempo = base?.aTiempo ?: 0
    val porcentaje = calcularIndicador(considerados, aTiempo)
    return IndicadorCumplimiento(considerados, aTiempo, porcentaje, categoriaCumplimiento(porcentaje))
}

fun BaseDatos.reconocimientosDe(idUsuario: Int): List<UsuarioReconocimiento> =
    reconocimientosObtenidos.filter { it.idUsuarioFinal == idUsuario }.sortedByDescending { it.fechaObtencion }

fun BaseDatos.recomendacionesDe(idUsuario: Int): List<Recomendacion> =
    recomendaciones.filter { it.idUsuarioFinal == idUsuario }.sortedByDescending { it.fecha }

fun BaseDatos.tarjetasGuardadas(idUsuario: Int, medio: MedioPago? = null): List<Tarjeta> =
    tarjetas
        .filter { it.idUsuario == idUsuario && it.guardada && (medio == null || it.medioPago == medio) }
        .sortedByDescending { it.fechaRegistro }

fun BaseDatos.avisosProgramados(
    usuario: Usuario,
    activos: Boolean,
    anticipacion: DiasAnticipacion,
    horaActual: LocalTime = LocalTime.of(9, 0)
): List<AvisoProgramado> {
    if (!activos) return emptyList()
    return compromisosVigentes(usuario.idUsuario)
        .filter { it.estado == EstadoCompromiso.PENDIENTE || it.estado == EstadoCompromiso.VENCIDO }
        .map { c ->
            val empresa = empresaPorId(c.idEmpresa)?.nombreFantasia ?: ""
            val fechaAviso = if (c.estado == EstadoCompromiso.VENCIDO) fechaHoy
            else c.fechaVencimiento.minusDays(anticipacion.dias.toLong()).let { if (it.isBefore(fechaHoy)) fechaHoy else it }
            val hora = if (fechaAviso == fechaHoy) horaActual else LocalTime.of(9, 0)
            val mensaje = when {
                c.estado == EstadoCompromiso.VENCIDO ->
                    "${primerNombre(usuario)}, te falta \"${c.concepto}\" de $empresa para quedar al día. Puedes dejarlo listo en un minuto."
                c.fechaVencimiento == fechaHoy ->
                    "Hoy es el último día sin recargo para \"${c.concepto}\" de $empresa."
                else ->
                    "${primerNombre(usuario)}, \"${c.concepto}\" de $empresa vence el ${formatoFecha(c.fechaVencimiento)}. ¡Vas bien!"
            }
            AvisoProgramado(c, ajustarHorarioAviso(LocalDateTime.of(fechaAviso, hora)), mensaje)
        }
        .sortedBy { it.fechaEntrega }
}

fun BaseDatos.reportesOrdenados(filtro: EstadoReporte?): List<Reporte> =
    reportes
        .filter { filtro == null || it.estado == filtro }
        .sortedWith(
            compareByDescending<Reporte> { reporteAtrasado(it, fechaHoy) }
                .thenBy { it.estado.ordinal }
                .thenBy { it.fechaCreacion }
        )

fun BaseDatos.usuarioDeReporte(reporte: Reporte): Usuario? =
    usuarioPorId(compromisoPorId(reporte.idCompromiso)?.idUsuarioFinal)

fun BaseDatos.cantidadReportesAtrasados(): Int = reportes.count { reporteAtrasado(it, fechaHoy) }

fun BaseDatos.recomendacionesOrdenadas(): List<Recomendacion> =
    recomendaciones.sortedWith(compareBy<Recomendacion> { it.estado.orden }.thenBy { it.fecha })

fun BaseDatos.historialDe(idRecomendacion: Int): List<HistorialRecomendacion> =
    historialRecomendaciones.filter { it.idRecomendacion == idRecomendacion }.sortedBy { it.fecha }

fun BaseDatos.contactoDe(idRecomendacion: Int): ContactoRecomendacion? =
    contactosRecomendacion.find { it.idRecomendacion == idRecomendacion }

fun BaseDatos.clientes(texto: String = ""): List<Usuario> {
    val buscado = texto.trim().lowercase()
    val rutBuscado = texto.filter { it.isLetterOrDigit() }.lowercase()
    return usuarios
        .filter { it.rol == Rol.USUARIO_FINAL }
        .filter { u ->
            buscado.isEmpty() ||
                nombreCompleto(u).lowercase().contains(buscado) ||
                u.correo.lowercase().contains(buscado) ||
                (rutBuscado.isNotEmpty() && u.rut.filter { it.isLetterOrDigit() }.lowercase().contains(rutBuscado))
        }
        .sortedWith(compareBy<Usuario> { it.apellidos }.thenBy { it.nombres })
}

fun BaseDatos.compromisosDe(idUsuario: Int): List<Compromiso> =
    compromisos.filter { it.idUsuarioFinal == idUsuario }.sortedBy { it.fechaVencimiento }

fun BaseDatos.deudaDe(idUsuario: Int): Int =
    compromisosDe(idUsuario)
        .filter { it.estado == EstadoCompromiso.PENDIENTE || it.estado == EstadoCompromiso.VENCIDO }
        .sumOf { totalAPagar(it) }

fun BaseDatos.compromisosEmpresa(idEmpresa: Int): List<Compromiso> =
    compromisos.filter { it.idEmpresa == idEmpresa }

fun BaseDatos.mesesVencimiento(idEmpresa: Int? = null): List<YearMonth> =
    compromisos
        .filter { idEmpresa == null || it.idEmpresa == idEmpresa }
        .map { YearMonth.from(it.fechaVencimiento) }.distinct().sorted()

fun BaseDatos.mesesCobranza(idEmpresa: Int): List<YearMonth> =
    (mesesVencimiento(idEmpresa) + DatosAnexo9.MES_RESUMEN_ANEXO).distinct().sorted()

fun BaseDatos.resumenCobranza(idEmpresa: Int, mes: YearMonth): ResumenCobranza {
    val agregado = DatosAnexo9.resumenCobranzaSeptiembre.find { it.idEmpresa == idEmpresa && it.mes == mes }
    if (agregado != null) {
        return ResumenCobranza(
            mes = mes,
            total = agregado.total,
            anulados = agregado.anulados,
            pagados = agregado.pagados,
            pendientes = 0,
            vencidos = agregado.vencidos,
            enRevision = 0,
            montoRecaudado = agregado.montoRecaudado,
            tasa = tasaCobranza(agregado.pagados, agregado.total, agregado.anulados),
            esCifraAgregada = true
        )
    }
    val delMes = compromisosEmpresa(idEmpresa).filter { YearMonth.from(it.fechaVencimiento) == mes }
    val anulados = delMes.count { it.estado == EstadoCompromiso.ANULADO }
    val pagados = delMes.count { it.estado == EstadoCompromiso.PAGADO }
    return ResumenCobranza(
        mes = mes,
        total = delMes.size,
        anulados = anulados,
        pagados = pagados,
        pendientes = delMes.count { it.estado == EstadoCompromiso.PENDIENTE },
        vencidos = delMes.count { it.estado == EstadoCompromiso.VENCIDO },
        enRevision = delMes.count { it.estado == EstadoCompromiso.EN_REVISION },
        montoRecaudado = delMes.mapNotNull { pagoDe(it.idCompromiso) }.sumOf { it.montoPagado.toLong() },
        tasa = tasaCobranza(pagados, delMes.size, anulados),
        esCifraAgregada = false
    )
}

fun BaseDatos.vencidosEmpresa(idEmpresa: Int): List<Compromiso> =
    compromisosEmpresa(idEmpresa)
        .filter { it.estado == EstadoCompromiso.VENCIDO }
        .sortedByDescending { diasAtraso(it, fechaHoy) }

fun BaseDatos.motivoParaNoPagar(idCompromiso: Int, sinConexion: Boolean = false): String? {
    val compromiso = compromisoPorId(idCompromiso) ?: return "No encontramos este compromiso"
    return when (compromiso.estado) {
        EstadoCompromiso.PAGADO ->
            "Este compromiso ya está pagado (puede haberse pagado en el portal o con el agente conversacional). Su comprobante está en tu historial."
        EstadoCompromiso.ANULADO ->
            "La empresa anuló este compromiso, así que no necesitas pagarlo."
        EstadoCompromiso.EN_REVISION ->
            "Este compromiso está en revisión por un reporte. Podrás pagarlo cuando se resuelva."
        else -> compromisoQueDebePagarseAntes(compromiso)?.let { anterior ->
            val empresa = empresaPorId(anterior.idEmpresa)?.nombreFantasia ?: ""
            val queCosa = if (anterior.numeroCuota != null && anterior.totalCuotas != null)
                "cuota ${anterior.numeroCuota}/${anterior.totalCuotas}" else "\"${anterior.concepto}\""
            "Primero paga la $queCosa de $empresa, que vence el ${formatoFecha(anterior.fechaVencimiento)}. Así tus cuotas quedan en orden."
        } ?: if (sinConexion)
            "No pudimos confirmar el pago porque no hay conexión. No se hizo ningún cobro y tu compromiso sigue igual."
        else null
    }
}

fun BaseDatos.motivoParaNoReportar(idCompromiso: Int): String? {
    val compromiso = compromisoPorId(idCompromiso) ?: return "No encontramos este compromiso"
    val activo = reporteActivoDe(idCompromiso)
    return when {
        activo != null ->
            "Ya enviaste un reporte por este cobro y está ${activo.estado.etiqueta.lowercase()}. Te avisaremos cuando se resuelva."
        compromiso.estado != EstadoCompromiso.PENDIENTE && compromiso.estado != EstadoCompromiso.VENCIDO ->
            "Solo puedes reportar compromisos pendientes o vencidos"
        else -> null
    }
}

fun BaseDatos.motivoParaNoTomar(idReporte: Int, admin: Usuario?): String? {
    val reporte = reportePorId(idReporte) ?: return "No encontramos el reporte"
    return when {
        admin == null || admin.rol != Rol.ADMINISTRADOR -> "Inicia sesión como administrador"
        reporte.estado != EstadoReporte.ABIERTO -> "Este reporte ya está ${reporte.estado.etiqueta.lowercase()}"
        else -> null
    }
}

fun BaseDatos.motivoParaNoResolver(idReporte: Int, admin: Usuario?, respuesta: String): String? {
    val reporte = reportePorId(idReporte) ?: return "No encontramos el reporte"
    return when {
        admin == null || admin.rol != Rol.ADMINISTRADOR -> "Inicia sesión como administrador"
        reporte.estado == EstadoReporte.ABIERTO -> "Primero debes tomar el reporte"
        reporte.estado == EstadoReporte.RESUELTO -> "Este reporte ya está resuelto"
        reporte.idAdministrador != admin.idUsuario -> "Solo el administrador responsable puede resolverlo"
        respuesta.isBlank() -> "Escribe la respuesta para el usuario antes de resolver"
        respuesta.length > 500 -> "Máximo 500 caracteres"
        else -> null
    }
}

fun BaseDatos.motivoParaNoCambiarEstado(idRecomendacion: Int, nuevo: EstadoRecomendacion, motivo: String?): String? {
    val recomendacion = recomendacionPorId(idRecomendacion) ?: return "No encontramos la recomendación"
    val actual = recomendacion.estado
    return when {
        actual == EstadoRecomendacion.SE_UNIO || actual == EstadoRecomendacion.DESCARTADA ->
            "Esta recomendación ya terminó (${actual.etiqueta})"
        nuevo == actual -> "La recomendación ya está en ese estado"
        nuevo != EstadoRecomendacion.DESCARTADA && nuevo.orden != actual.orden + 1 -> {
            val siguiente = EstadoRecomendacion.entries.first { it.orden == actual.orden + 1 }
            "No se pueden saltar etapas. El siguiente paso es \"${siguiente.etiqueta}\""
        }
        nuevo == EstadoRecomendacion.DESCARTADA && motivo.isNullOrBlank() -> "Indica el motivo del descarte"
        else -> null
    }
}

fun BaseDatos.motivoParaNoAnular(admin: Usuario?, idCompromiso: Int, motivo: MotivoAnulacion, comentario: String?): String? {
    val compromiso = compromisoPorId(idCompromiso) ?: return "No encontramos el compromiso"
    return when {
        admin == null || admin.rol != Rol.ADMINISTRADOR -> "Solo un administrador puede anular compromisos"
        compromiso.estado == EstadoCompromiso.PAGADO -> "Un compromiso pagado no se puede anular"
        compromiso.estado == EstadoCompromiso.ANULADO -> "Este compromiso ya está anulado"
        motivo == MotivoAnulacion.OTRO && comentario.isNullOrBlank() ->
            "El comentario es obligatorio cuando el motivo es \"Otro\""
        (comentario?.length ?: 0) > 255 -> "Máximo 255 caracteres"
        else -> null
    }
}

fun BaseDatos.motivoParaNoEditar(idCompromiso: Int): String? {
    val compromiso = compromisoPorId(idCompromiso) ?: return "No encontramos el compromiso"
    return when (compromiso.estado) {
        EstadoCompromiso.PAGADO -> "Un compromiso pagado no se modifica: su comprobante ya fue emitido"
        EstadoCompromiso.ANULADO -> "Un compromiso anulado no se modifica"
        EstadoCompromiso.EN_REVISION -> "Tiene un reporte abierto: resuélvelo primero en \"Reportes\""
        else -> null
    }
}

data class ErroresPersona(
    val rut: String? = null,
    val nombres: String? = null,
    val apellidos: String? = null,
    val correo: String? = null,
    val telefono: String? = null
) {
    val hayErrores: Boolean get() = listOf(rut, nombres, apellidos, correo, telefono).any { it != null }
}

fun BaseDatos.validarPersona(
    rut: String,
    nombres: String,
    apellidos: String,
    correo: String,
    telefono: String,
    idPropio: Int? = null
): ErroresPersona {
    val conMismoRut = if (validarRut(rut)) usuarioPorRut(rut) else null
    val conMismoCorreo = usuarioPorCorreo(correo)
    return ErroresPersona(
        rut = when {
            rut.isBlank() -> "El RUT es obligatorio"
            !validarRut(rut) -> "El RUT no es válido: revisa el dígito verificador"
            conMismoRut != null && conMismoRut.idUsuario != idPropio -> "Ya existe una cuenta con ese RUT"
            else -> null
        },
        nombres = when {
            nombres.isBlank() -> "Ingresa los nombres"
            nombres.any { it.isDigit() } -> "Los nombres no llevan números"
            else -> null
        },
        apellidos = when {
            apellidos.isBlank() -> "Ingresa los apellidos"
            apellidos.any { it.isDigit() } -> "Los apellidos no llevan números"
            else -> null
        },
        correo = when {
            correo.isBlank() -> "Ingresa el correo"
            !correoValido(correo) -> "Ingresa un correo válido, por ejemplo nombre@gmail.com"
            conMismoCorreo != null && conMismoCorreo.idUsuario != idPropio -> "Ese correo ya está registrado en PagoFlex"
            else -> null
        },
        telefono = if (!telefonoValido(telefono)) "Ingresa un teléfono de al menos 8 dígitos" else null
    )
}

fun BaseDatos.cuentaParaRecuperar(correo: String, rut: String): Usuario? =
    usuarioPorCorreo(correo)?.takeIf { validarRut(rut) && normalizarRut(it.rut) == normalizarRut(rut) }

fun BaseDatos.siguienteCuota(compromiso: Compromiso, fecha: LocalDateTime): Compromiso? {
    val numero = compromiso.numeroCuota ?: return null
    val total = compromiso.totalCuotas ?: return null
    if (numero >= total) return null
    val yaExiste = compromisos.any {
        it.idUsuarioFinal == compromiso.idUsuarioFinal && it.idEmpresa == compromiso.idEmpresa &&
            it.totalCuotas == total && it.numeroCuota == numero + 1 && it.estado != EstadoCompromiso.ANULADO
    }
    if (yaExiste) return null
    val nuevoId = compromisos.maxOf { it.idCompromiso } + 1
    val siguiente = Compromiso(
        idCompromiso = nuevoId,
        folio = "CP-%d-%04d".format(fechaHoy.year, nuevoId),
        idUsuarioFinal = compromiso.idUsuarioFinal,
        idEmpresa = compromiso.idEmpresa,
        concepto = conceptoSiguiente(compromiso.concepto),
        numeroCuota = numero + 1,
        totalCuotas = total,
        monto = compromiso.monto,
        fechaVencimiento = compromiso.fechaVencimiento.plusMonths(1),
        estado = EstadoCompromiso.PENDIENTE,
        recargoAplicado = 0,
        fechaCambioEstado = fecha,
        idAdminRegistro = compromiso.idAdminRegistro,
        fechaRegistro = fecha
    )
    return actualizarVencimientos(listOf(siguiente), empresas, fechaHoy).first()
}

fun BaseDatos.avancePorEmpresa(idUsuario: Int): List<AvanceEmpresa> =
    compromisos
        .filter { it.idUsuarioFinal == idUsuario && it.estado != EstadoCompromiso.ANULADO }
        .groupBy { it.idEmpresa }
        .mapNotNull { (idEmpresa, lista) ->
            val empresa = empresaPorId(idEmpresa) ?: return@mapNotNull null
            val vigentes = lista
                .filter {
                    it.estado == EstadoCompromiso.PENDIENTE || it.estado == EstadoCompromiso.VENCIDO ||
                        it.estado == EstadoCompromiso.EN_REVISION
                }
                .sortedBy { it.fechaVencimiento }
            AvanceEmpresa(
                empresa = empresa,
                actual = vigentes.firstOrNull(),
                porPagar = vigentes.size,
                ultimoPagado = lista
                    .filter { it.estado == EstadoCompromiso.PAGADO }
                    .maxWithOrNull(compareBy<Compromiso> { it.fechaVencimiento }.thenBy { it.numeroCuota ?: 0 })
            )
        }
        .sortedWith(
            compareBy<AvanceEmpresa> { it.alDia }
                .thenBy { it.actual?.fechaVencimiento }
                .thenBy { it.empresa.nombreFantasia }
        )

fun BaseDatos.resumenPanel(meses: Int = 4): ResumenPanel {
    val porCobrar = compromisos.filter { it.estado == EstadoCompromiso.PENDIENTE || it.estado == EstadoCompromiso.VENCIDO }
    val vencidos = porCobrar.filter { it.estado == EstadoCompromiso.VENCIDO }
    val mesActual = YearMonth.from(fechaHoy)
    val pagosPorMes = pagos.groupBy { YearMonth.from(it.fechaHora) }
    val pagosDelMes = pagosPorMes[mesActual].orEmpty()
    val clientes = usuarios.filter { it.rol == Rol.USUARIO_FINAL }
    return ResumenPanel(
        montoPorCobrar = porCobrar.sumOf { totalAPagar(it).toLong() },
        cantidadPorCobrar = porCobrar.size,
        montoVencido = vencidos.sumOf { totalAPagar(it).toLong() },
        cantidadVencidos = vencidos.size,
        recaudadoMes = pagosDelMes.sumOf { it.montoPagado.toLong() },
        pagosMes = pagosDelMes.size,
        clientesActivos = clientes.count { it.activo },
        clientesConDeuda = porCobrar.map { it.idUsuarioFinal }.distinct().size,
        reportesAbiertos = reportes.count { it.estado != EstadoReporte.RESUELTO },
        reportesAtrasados = cantidadReportesAtrasados(),
        recomendacionesEnCurso = recomendaciones.count {
            it.estado != EstadoRecomendacion.SE_UNIO && it.estado != EstadoRecomendacion.DESCARTADA
        },
        porEstado = EstadoCompromiso.entries.map { estado -> CantidadEstado(estado, compromisos.count { it.estado == estado }) },
        recaudacionPorMes = (meses - 1 downTo 0).map { atras ->
            val mes = mesActual.minusMonths(atras.toLong())
            RecaudacionMes(mes, pagosPorMes[mes].orEmpty().sumOf { it.montoPagado.toLong() })
        },
        deudaPorEmpresa = porCobrar
            .groupBy { it.idEmpresa }
            .mapNotNull { (idEmpresa, lista) ->
                empresaPorId(idEmpresa)?.let { empresa ->
                    DeudaEmpresa(empresa, lista.sumOf { totalAPagar(it).toLong() }, lista.size)
                }
            }
            .sortedByDescending { it.monto },
        vencidosAntiguos = vencidos.sortedByDescending { diasAtraso(it, fechaHoy) }.take(3),
        proximosVencimientos = porCobrar
            .filter {
                it.estado == EstadoCompromiso.PENDIENTE && !it.fechaVencimiento.isBefore(fechaHoy) &&
                    it.fechaVencimiento.isBefore(fechaHoy.plusDays(8))
            }
            .sortedBy { it.fechaVencimiento }
            .take(5)
    )
}
