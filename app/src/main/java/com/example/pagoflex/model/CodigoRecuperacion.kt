package com.example.pagoflex.model

import java.security.SecureRandom
import java.time.LocalDateTime

const val LARGO_CODIGO_RECUPERACION = 6

const val MINUTOS_VIGENCIA_CODIGO = 10L

const val INTENTOS_MAXIMOS_CODIGO = 3

data class CodigoRecuperacion(
    val idCodigo: Int,
    val idUsuario: Int,
    val codigoHash: String,
    val fechaCreacion: LocalDateTime,
    val fechaExpiracion: LocalDateTime,
    val intentos: Int = 0,
    val usado: Boolean = false
)

enum class ResultadoCodigo { CORRECTO, INCORRECTO, VENCIDO, SIN_INTENTOS, SIN_CODIGO }

private val generadorSeguro = SecureRandom()

fun generarCodigoRecuperacion(): String =
    generadorSeguro.nextInt(1_000_000).toString().padStart(LARGO_CODIGO_RECUPERACION, '0')

fun hashCodigo(idUsuario: Int, codigo: String): String = sha256("$idUsuario:$codigo")

fun nuevoCodigoRecuperacion(idUsuario: Int, codigo: String, ahora: LocalDateTime): CodigoRecuperacion =
    CodigoRecuperacion(
        idCodigo = 0,
        idUsuario = idUsuario,
        codigoHash = hashCodigo(idUsuario, codigo),
        fechaCreacion = ahora,
        fechaExpiracion = ahora.plusMinutes(MINUTOS_VIGENCIA_CODIGO)
    )

fun revisarCodigo(codigo: CodigoRecuperacion?, ingresado: String, ahora: LocalDateTime): ResultadoCodigo = when {
    codigo == null || codigo.usado -> ResultadoCodigo.SIN_CODIGO
    codigo.intentos >= INTENTOS_MAXIMOS_CODIGO -> ResultadoCodigo.SIN_INTENTOS
    ahora.isAfter(codigo.fechaExpiracion) -> ResultadoCodigo.VENCIDO
    hashCodigo(codigo.idUsuario, ingresado) != codigo.codigoHash -> ResultadoCodigo.INCORRECTO
    else -> ResultadoCodigo.CORRECTO
}

fun mensajeCodigo(resultado: ResultadoCodigo, intentosRestantes: Int = 0): String? = when (resultado) {
    ResultadoCodigo.CORRECTO -> null
    ResultadoCodigo.INCORRECTO -> when {
        intentosRestantes > 1 -> "El código no es correcto. Te quedan $intentosRestantes intentos."
        intentosRestantes == 1 -> "El código no es correcto. Te queda 1 intento."
        else -> "El código no es correcto y ya no te quedan intentos. Solicita un código nuevo."
    }
    ResultadoCodigo.VENCIDO -> "El código venció: dura $MINUTOS_VIGENCIA_CODIGO minutos. Solicita uno nuevo."
    ResultadoCodigo.SIN_INTENTOS -> "Superaste los $INTENTOS_MAXIMOS_CODIGO intentos. Solicita un código nuevo."
    ResultadoCodigo.SIN_CODIGO -> "El código no es correcto o ya no es válido. Solicita uno nuevo."
}

fun errorFormatoCodigo(codigo: String): String? = when {
    codigo.isBlank() -> "Ingresa el código que te enviamos"
    codigo.length != LARGO_CODIGO_RECUPERACION || !codigo.all { it.isDigit() } ->
        "El código tiene $LARGO_CODIGO_RECUPERACION números"
    else -> null
}
