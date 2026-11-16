package com.example.pagoflex.model

import java.time.DateTimeException
import java.time.LocalDate

data class LecturaBoleta(
    val monto: Int? = null,
    val vencimiento: LocalDate? = null,
    val idEmpresa: Int? = null,
    val concepto: String? = null
) {
    val encontroAlgo: Boolean get() = monto != null || vencimiento != null || idEmpresa != null
}

private val patronMontoConSigno = Regex("""\$\s*([0-9]{1,3}(?:\.[0-9]{3})+|[0-9]{2,9})""")
private val patronMontoSinSigno = Regex("""(?<![0-9.\-/])([0-9]{1,3}(?:\.[0-9]{3})+|[0-9]{3,9})(?![0-9\-/])""")
private val patronFechaNumerica = Regex("""(?<![0-9])([0-3]?[0-9])[-/.]([01]?[0-9])[-/.]((?:20)?[0-9]{2})(?![0-9])""")
private val patronFechaEnPalabras = Regex("""(?<![0-9])([0-3]?[0-9])\s+de\s+([a-záéíóú]+)\s+(?:de\s+)?(20[0-9]{2})""")

private val palabrasMonto = listOf("total a pagar", "monto a pagar", "total", "monto", "a pagar")
private val palabrasVencimiento = listOf("vencimiento", "vence", "pagar hasta", "fecha límite", "fecha limite", "fecha de pago")
private val mesesEnPalabras = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

private fun aNumero(texto: String): Int? = texto.replace(".", "").toIntOrNull()?.takeIf { it > 0 }

fun montosEn(linea: String, conPalabraClave: Boolean = false): List<Int> {
    val conSigno = patronMontoConSigno.findAll(linea).mapNotNull { aNumero(it.groupValues[1]) }.toList()
    if (conSigno.isNotEmpty() || !conPalabraClave) return conSigno
    return patronMontoSinSigno.findAll(linea).mapNotNull { aNumero(it.groupValues[1]) }.toList()
}

private fun fecha(dia: Int, mes: Int, anio: Int): LocalDate? = try {
    LocalDate.of(if (anio < 100) 2000 + anio else anio, mes, dia)
} catch (e: DateTimeException) {
    null
}

fun fechasEn(linea: String): List<LocalDate> {
    val minuscula = linea.lowercase()
    val numericas = patronFechaNumerica.findAll(minuscula).mapNotNull { m ->
        fecha(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())
    }
    val enPalabras = patronFechaEnPalabras.findAll(minuscula).mapNotNull { m ->
        val mes = mesesEnPalabras.indexOf(m.groupValues[2]) + 1
        if (mes == 0) null else fecha(m.groupValues[1].toInt(), mes, m.groupValues[3].toInt())
    }
    return (numericas + enPalabras).toList()
}

private fun <T> buscarCercaDe(lineas: List<String>, palabras: List<String>, leer: (String) -> List<T>): T? {
    for (palabra in palabras) {
        for ((indice, linea) in lineas.withIndex()) {
            val posicion = linea.lowercase().indexOf(palabra)
            if (posicion < 0) continue
            val enLinea = leer(linea.substring(posicion))
            if (enLinea.isNotEmpty()) return enLinea.first()
            val siguiente = lineas.getOrNull(indice + 1)?.let(leer).orEmpty()
            if (siguiente.isNotEmpty()) return siguiente.first()
        }
    }
    return null
}

private fun soloRut(texto: String): String = texto.filter { it.isDigit() || it == 'k' || it == 'K' }.lowercase()

fun empresaEnTexto(texto: String, empresas: List<EmpresaCliente>): EmpresaCliente? {
    val minuscula = texto.lowercase()
    val rutDelTexto = soloRut(texto)
    return empresas.firstOrNull { empresa ->
        minuscula.contains(empresa.nombreFantasia.lowercase()) ||
            minuscula.contains(empresa.razonSocial.lowercase()) ||
            rutDelTexto.contains(soloRut(empresa.rut))
    }
}

fun leerBoleta(texto: String, empresas: List<EmpresaCliente>, hoy: LocalDate): LecturaBoleta {
    val lineas = texto.lines().map { it.trim() }.filter { it.isNotEmpty() }
    if (lineas.isEmpty()) return LecturaBoleta()

    val monto = buscarCercaDe(lineas, palabrasMonto) { montosEn(it, conPalabraClave = true) }
        ?: lineas.flatMap { montosEn(it) }.maxOrNull()

    val vencimiento = buscarCercaDe(lineas, palabrasVencimiento) { fechasEn(it) }
        ?: lineas.flatMap { fechasEn(it) }.filter { !it.isBefore(hoy) }.maxOrNull()

    val empresa = empresaEnTexto(texto, empresas)
    val concepto = when {
        vencimiento != null -> "Boleta de ${mesesEnPalabras[vencimiento.monthValue - 1]} ${vencimiento.year}"
        empresa != null -> "Boleta ${empresa.nombreFantasia}"
        else -> null
    }
    return LecturaBoleta(monto = monto, vencimiento = vencimiento, idEmpresa = empresa?.idEmpresa, concepto = concepto)
}

fun mensajeLectura(lectura: LecturaBoleta?, empresas: List<EmpresaCliente>): String = when {
    lectura == null -> "No pudimos leer la foto. Completa los datos a mano."
    !lectura.encontroAlgo -> "No encontramos el monto ni la fecha en la foto. Prueba con más luz o escríbelos a mano."
    else -> "Leímos ${resumenLectura(lectura, empresas)}. Revisa los datos antes de agregar."
}

fun resumenLectura(lectura: LecturaBoleta, empresas: List<EmpresaCliente>): String = listOfNotNull(
    lectura.monto?.let { "monto ${formatoPesos(it)}" },
    lectura.vencimiento?.let { "vence el ${formatoFecha(it)}" },
    lectura.idEmpresa?.let { id -> empresas.firstOrNull { it.idEmpresa == id }?.nombreFantasia }
).joinToString(", ")
