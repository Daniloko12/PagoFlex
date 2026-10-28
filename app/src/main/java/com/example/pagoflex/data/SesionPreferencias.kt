package com.example.pagoflex.data

import android.content.Context
import androidx.core.content.edit
import java.time.LocalDateTime

class SesionPreferencias(context: Context) {
    private val prefs = context.getSharedPreferences(NOMBRE_ARCHIVO, Context.MODE_PRIVATE)

    companion object {
        const val NOMBRE_ARCHIVO = "sesion"
        private const val CLAVE_ID_USUARIO = "id_usuario"
        private const val CLAVE_SIN_CONEXION = "sin_conexion"
        private const val CLAVE_ULTIMA_ACTUALIZACION = "ultima_actualizacion"
        private const val CLAVE_MODO_OSCURO = "modo_oscuro"
        private const val SIN_SESION = -1
    }

    fun guardar(idUsuario: Int) {
        prefs.edit { putInt(CLAVE_ID_USUARIO, idUsuario) }
    }

    fun leer(): Int = prefs.getInt(CLAVE_ID_USUARIO, SIN_SESION)

    fun borrar() {
        prefs.edit { remove(CLAVE_ID_USUARIO) }
    }

    fun guardarSinConexion(sinConexion: Boolean) {
        prefs.edit { putBoolean(CLAVE_SIN_CONEXION, sinConexion) }
    }

    fun leerSinConexion(): Boolean = prefs.getBoolean(CLAVE_SIN_CONEXION, false)

    fun guardarUltimaActualizacion(fechaHora: LocalDateTime) {
        prefs.edit { putString(CLAVE_ULTIMA_ACTUALIZACION, fechaHora.toString()) }
    }

    fun leerUltimaActualizacion(): LocalDateTime? =
        prefs.getString(CLAVE_ULTIMA_ACTUALIZACION, null)?.let { LocalDateTime.parse(it) }

    fun guardarModoOscuro(oscuro: Boolean) {
        prefs.edit { putBoolean(CLAVE_MODO_OSCURO, oscuro) }
    }

    fun leerModoOscuro(): Boolean = prefs.getBoolean(CLAVE_MODO_OSCURO, true)
}
