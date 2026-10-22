package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.NuevaCuenta
import com.example.pagoflex.model.RegistroUiState
import com.example.pagoflex.model.errorClaveNueva
import com.example.pagoflex.model.errorConfirmacionClave
import com.example.pagoflex.model.validarPersona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegistroViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null, errorRegistro = null) }
    }

    fun onNombresCambia(valor: String) {
        _uiState.update { it.copy(nombres = valor, errorNombres = null) }
    }

    fun onApellidosCambia(valor: String) {
        _uiState.update { it.copy(apellidos = valor, errorApellidos = null) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, errorRegistro = null) }
    }

    fun onTelefonoCambia(valor: String) {
        _uiState.update { it.copy(telefono = valor, errorTelefono = null) }
    }

    fun onClaveCambia(valor: String) {
        _uiState.update { it.copy(clave = valor.filter { c -> c.isDigit() }.take(5), errorClave = null) }
    }

    fun onConfirmacionCambia(valor: String) {
        _uiState.update { it.copy(confirmacionClave = valor.filter { c -> c.isDigit() }.take(5), errorConfirmacion = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(claveVisible = !it.claveVisible) }
    }

    fun onAceptaTerminosCambia(valor: Boolean) {
        _uiState.update { it.copy(aceptaTerminos = valor, errorTerminos = null) }
    }

    fun onErrorRegistro(mensaje: String) {
        _uiState.update { it.copy(errorRegistro = mensaje) }
    }

    fun onCrearCuenta(datos: BaseDatos): NuevaCuenta? {
        val estado = _uiState.value
        val errores = datos.validarPersona(estado.rut, estado.nombres, estado.apellidos, estado.correo, estado.telefono)
        val errorClave = errorClaveNueva(estado.clave)
        val errorConfirmacion = errorConfirmacionClave(estado.clave, estado.confirmacionClave)
        val errorTerminos = if (!estado.aceptaTerminos) "Debes aceptar los términos para crear tu cuenta" else null
        val hayErrores = errores.hayErrores || listOf(errorClave, errorConfirmacion, errorTerminos).any { it != null }

        _uiState.update {
            it.copy(
                errorRut = errores.rut,
                errorNombres = errores.nombres,
                errorApellidos = errores.apellidos,
                errorCorreo = errores.correo,
                errorTelefono = errores.telefono,
                errorClave = errorClave,
                errorConfirmacion = errorConfirmacion,
                errorTerminos = errorTerminos,
                errorRegistro = null
            )
        }
        if (hayErrores) return null

        return NuevaCuenta(
            rut = estado.rut.trim(),
            nombres = estado.nombres.trim(),
            apellidos = estado.apellidos.trim(),
            correo = estado.correo.trim(),
            telefono = estado.telefono.trim(),
            clave = estado.clave
        )
    }

    fun onLimpiar() {
        _uiState.value = RegistroUiState()
    }
}
