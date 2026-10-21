package com.example.pagoflex.model

import java.security.MessageDigest
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.time.temporal.ChronoUnit

private val formatoFechaCorta: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
private val formatoFechaHoraCorta: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
private val formatoEntrada: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)

private val nombresMeses = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

fun formatoPesos(monto: Long): String {
    val miles = monto.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$\u00A0$miles"
}

fun formatoPesos(monto: Int): String = formatoPesos(monto.toLong())

fun formatoFecha(fecha: LocalDate): String = fecha.format(formatoFechaCorta)

fun formatoFechaHora(fechaHora: LocalDateTime): String = fechaHora.format(formatoFechaHoraCorta)

fun parsearFecha(texto: String): LocalDate? = try {
    LocalDate.parse(texto.trim(), formatoEntrada)
} catch (e: DateTimeParseException) {
    null
}

fun nombreMes(mes: Int): String = nombresMeses[mes - 1]

fun etiquetaMes(mes: YearMonth): String = "${nombreMes(mes.monthValue)} ${mes.year}"

fun formatoPorcentaje(valor: Double): String {
    val decimas = Math.round(valor * 10)
    return "${decimas / 10},${decimas % 10}%"
}

private fun redondearUnDecimal(valor: Double): Double = Math.round(valor * 10) / 10.0

fun validarRut(rut: String): Boolean {
    val limpio = rut.replace(".", "").replace("-", "").replace(" ", "").uppercase()
    if (limpio.length < 2) return false
    val cuerpo = limpio.dropLast(1)
    val dv = limpio.last()
    if (cuerpo.length > 8 || !cuerpo.all { it.isDigit() }) return false
    var suma = 0
    var multiplicador = 2
    for (digito in cuerpo.reversed()) {
        suma += (digito - '0') * multiplicador
        multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
    }
    val resto = 11 - (suma % 11)
    val dvEsperado = when (resto) {
        11 -> '0'
        10 -> 'K'
        else -> ('0' + resto)
    }
    return dv == dvEsperado
}

fun normalizarRut(rut: String): String {
    val limpio = rut.replace(".", "").replace("-", "").replace(" ", "").uppercase()
    if (limpio.length < 2) return rut.trim()
    val cuerpo = limpio.dropLast(1)
    val dv = limpio.last()
    val cuerpoConPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
    return "$cuerpoConPuntos-$dv"
}

fun ocultarRut(rut: String): String {
    val normal = normalizarRut(rut)
    val primerGrupo = normal.substringBefore(".")
    val dv = normal.substringAfterLast("-")
    return "$primerGrupo.•••.•••-$dv"
}

fun codigoConfirmacionDe(rut: String): String {
    val cuerpo = rut.replace(".", "").replace(" ", "").substringBefore("-")
    return cuerpo.takeLast(4)
}

const val CLAVE_DE_PRUEBA = "12345"

fun claveValida(clave: String): Boolean = clave.length == 5 && clave.all { it.isDigit() }

fun hashClave(rut: String, clave: String): String {
    val texto = "${normalizarRut(rut)}:$clave"
    val bytes = MessageDigest.getInstance("SHA-256").digest(texto.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}

fun correoValido(correo: String): Boolean {
    val texto = correo.trim()
    val arroba = texto.indexOf('@')
    return arroba > 0 && texto.indexOf('.', arroba) > arroba + 1 && !texto.endsWith(".") && !texto.contains(" ")
}

fun telefonoValido(telefono: String): Boolean = telefono.count { it.isDigit() } >= 8

fun nombreCompleto(usuario: Usuario): String = "${usuario.nombres} ${usuario.apellidos}"

fun primerNombre(usuario: Usuario): String = usuario.nombres.substringBefore(" ")

fun textoCuota(compromiso: Compromiso): String? =
    if (compromiso.numeroCuota != null && compromiso.totalCuotas != null)
        "Cuota ${compromiso.numeroCuota}/${compromiso.totalCuotas}"
    else null

fun diasAtraso(compromiso: Compromiso, hoy: LocalDate): Int =
    if (hoy.isAfter(compromiso.fechaVencimiento))
        ChronoUnit.DAYS.between(compromiso.fechaVencimiento, hoy).toInt()
    else 0

fun totalAPagar(compromiso: Compromiso): Int =
    if (compromiso.estado == EstadoCompromiso.VENCIDO) compromiso.monto + compromiso.recargoAplicado
    else compromiso.monto

fun pagoATiempo(pago: Pago, compromiso: Compromiso): Boolean =
    !pago.fechaHora.toLocalDate().isAfter(compromiso.fechaVencimiento)

fun diasAtrasoDelPago(pago: Pago, compromiso: Compromiso): Int =
    diasAtraso(compromiso, pago.fechaHora.toLocalDate())

fun diasHabilesEntre(desde: LocalDate, hasta: LocalDate): Int {
    var contador = 0
    var dia = desde.plusDays(1)
    while (!dia.isAfter(hasta)) {
        if (dia.dayOfWeek != DayOfWeek.SATURDAY && dia.dayOfWeek != DayOfWeek.SUNDAY) contador++
        dia = dia.plusDays(1)
    }
    return contador
}

fun reporteAtrasado(reporte: Reporte, hoy: LocalDate): Boolean =
    reporte.estado == EstadoReporte.ABIERTO &&
        diasHabilesEntre(reporte.fechaCreacion.toLocalDate(), hoy) > 2

fun calcularIndicador(considerados: Int, aTiempo: Int): Double? =
    if (considerados < 3) null else redondearUnDecimal(aTiempo * 100.0 / considerados)

fun categoriaCumplimiento(porcentaje: Double?): CategoriaCumplimiento = when {
    porcentaje == null -> CategoriaCumplimiento.INSUFICIENTE
    porcentaje >= 95.0 -> CategoriaCumplimiento.EJEMPLAR
    porcentaje >= 80.0 -> CategoriaCumplimiento.AL_DIA
    else -> CategoriaCumplimiento.EN_CAMINO
}

fun mensajeCategoria(categoria: CategoriaCumplimiento): String = when (categoria) {
    CategoriaCumplimiento.EJEMPLAR -> "¡Excelente! Casi siempre pagas a tiempo."
    CategoriaCumplimiento.AL_DIA -> "Vas muy bien. Cada pago a tiempo suma."
    CategoriaCumplimiento.EN_CAMINO -> "Vas avanzando. Tu próximo pago a tiempo te acerca a \"Al día\"."
    CategoriaCumplimiento.INSUFICIENTE -> "Aún no tienes historial suficiente. Tu indicador aparecerá cuando tengas al menos 3 compromisos."
}

fun tasaCobranza(pagados: Int, total: Int, anulados: Int): Double? {
    val base = total - anulados
    return if (base <= 0) null else redondearUnDecimal(pagados * 100.0 / base)
}

fun ajustarHorarioAviso(fechaHora: LocalDateTime): LocalDateTime = when {
    fechaHora.hour < 8 -> fechaHora.toLocalDate().atTime(8, 0)
    fechaHora.hour >= 22 -> fechaHora.toLocalDate().plusDays(1).atTime(8, 0)
    else -> fechaHora
}

fun actualizarVencimientos(
    compromisos: List<Compromiso>,
    empresas: List<EmpresaCliente>,
    hoy: LocalDate
): List<Compromiso> = compromisos.map { c ->
    if (c.estado == EstadoCompromiso.PENDIENTE && hoy.isAfter(c.fechaVencimiento)) {
        val recargo = empresas.find { it.idEmpresa == c.idEmpresa }?.recargoAtraso ?: 0
        c.copy(
            estado = EstadoCompromiso.VENCIDO,
            recargoAplicado = if (c.recargoAplicado > 0) c.recargoAplicado else recargo,
            fechaCambioEstado = c.fechaVencimiento.plusDays(1).atStartOfDay()
        )
    } else c
}

fun luhnValido(numero: String): Boolean {
    val digitos = numero.filter { it.isDigit() }
    if (digitos.length !in 13..19) return false
    var suma = 0
    digitos.reversed().forEachIndexed { posicion, caracter ->
        var digito = caracter - '0'
        if (posicion % 2 == 1) {
            digito *= 2
            if (digito > 9) digito -= 9
        }
        suma += digito
    }
    return suma % 10 == 0
}

fun marcaDe(numero: String): MarcaTarjeta {
    val digitos = numero.filter { it.isDigit() }
    val dos = digitos.take(2).toIntOrNull() ?: 0
    val cuatro = digitos.take(4).toIntOrNull() ?: 0
    return when {
        digitos.startsWith("4") -> MarcaTarjeta.VISA
        dos in 51..55 || cuatro in 2221..2720 -> MarcaTarjeta.MASTERCARD
        dos == 34 || dos == 37 -> MarcaTarjeta.AMEX
        else -> MarcaTarjeta.OTRA
    }
}

fun parsearVencimientoTarjeta(texto: String): YearMonth? {
    val digitos = texto.filter { it.isDigit() }
    if (digitos.length != 4) return null
    val mes = digitos.take(2).toInt()
    val anio = 2000 + digitos.takeLast(2).toInt()
    return if (mes in 1..12) YearMonth.of(anio, mes) else null
}

fun formatoVencimiento(mes: YearMonth): String = "%02d/%02d".format(mes.monthValue, mes.year % 100)

fun formatoNumeroTarjeta(numero: String): String = numero.filter { it.isDigit() }.chunked(4).joinToString(" ")

fun textoTarjeta(tarjeta: Tarjeta): String = "${tarjeta.marca.etiqueta} •••• ${tarjeta.ultimos4}"

fun largoCvv(numero: String): Int = if (marcaDe(numero) == MarcaTarjeta.AMEX) 4 else 3

data class ErroresTarjeta(
    val numero: String? = null,
    val titular: String? = null,
    val vencimiento: String? = null,
    val cvv: String? = null
) {
    val hayErrores: Boolean get() = listOf(numero, titular, vencimiento, cvv).any { it != null }
}

fun validarTarjeta(numero: String, titular: String, vencimiento: String, cvv: String, hoy: LocalDate): ErroresTarjeta {
    val digitos = numero.filter { it.isDigit() }
    val mes = parsearVencimientoTarjeta(vencimiento)
    val largo = largoCvv(digitos)
    return ErroresTarjeta(
        numero = when {
            digitos.isEmpty() -> "Ingresa el número de la tarjeta"
            digitos.length < 13 -> "El número está incompleto"
            !luhnValido(digitos) -> "Revisa el número: no corresponde a una tarjeta válida"
            else -> null
        },
        titular = when {
            titular.isBlank() -> "Ingresa el nombre tal como aparece en la tarjeta"
            titular.any { it.isDigit() } -> "El nombre no lleva números"
            titular.trim().length < 3 -> "Escribe el nombre completo"
            else -> null
        },
        vencimiento = when {
            vencimiento.isBlank() -> "Ingresa el vencimiento"
            mes == null -> "Usa el formato MM/AA, por ejemplo 08/28"
            mes.isBefore(YearMonth.from(hoy)) -> "Esta tarjeta está vencida"
            else -> null
        },
        cvv = when {
            cvv.isBlank() -> "Ingresa el código de seguridad"
            cvv.length != largo -> "Son $largo dígitos, al reverso de la tarjeta"
            else -> null
        }
    )
}

fun tarjetaNuevaDe(medio: MedioPago, numero: String, titular: String, vencimiento: String): TarjetaNueva? {
    val digitos = numero.filter { it.isDigit() }
    val mes = parsearVencimientoTarjeta(vencimiento) ?: return null
    if (digitos.length < 4) return null
    return TarjetaNueva(
        medioPago = medio,
        marca = marcaDe(digitos),
        ultimos4 = digitos.takeLast(4),
        titular = titular.trim().uppercase(),
        vencimiento = mes
    )
}
