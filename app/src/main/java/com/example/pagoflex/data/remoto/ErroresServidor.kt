package com.example.pagoflex.data.remoto

import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException

const val MENSAJE_SIN_SERVIDOR =
    "No pudimos conectarnos con el servidor de PagoFlex. Revisa que los microservicios estén encendidos."

class ErrorServidor(mensaje: String) : Exception(mensaje)

fun mensajeDeError(error: Throwable): String = when (error) {
    is ErrorServidor -> error.message ?: MENSAJE_SIN_SERVIDOR
    is HttpException -> leerMensaje(error) ?: "El servidor respondió con un error (${error.code()}). Inténtalo de nuevo."
    is IOException -> MENSAJE_SIN_SERVIDOR
    else -> "Ocurrió un error inesperado. Inténtalo de nuevo."
}

private fun leerMensaje(error: HttpException): String? = try {
    error.response()?.errorBody()?.string()
        ?.let { ServidorPagoFlex.gson.fromJson(it, MensajeError::class.java) }
        ?.error
        ?.takeIf { it.isNotBlank() }
} catch (e: JsonParseException) {
    null
} catch (e: IOException) {
    null
}
