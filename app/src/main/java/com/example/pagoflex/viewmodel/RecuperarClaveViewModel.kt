package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.ClaveRecuperada
import com.example.pagoflex.model.DatosRecuperacion
import com.example.pagoflex.model.RecuperarClaveUiState
import com.example.pagoflex.model.correoValido
import com.example.pagoflex.model.errorClaveNueva
import com.example.pagoflex.model.errorConfirmacionClave
import com.example.pagoflex.model.validarRut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecuperarClaveViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecuperarClaveUiState())
    val uiState: StateFlow<RecuperarClaveUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), cuentaVerificada = false, errorCorreo = null, errorRecuperacion = null) }
    }

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, cuentaVerificada = false, errorRut = null, errorRecuperacion = null) }
    }

    fun onClaveNuevaCambia(valor: String) {
        _uiState.update { it.copy(claveNueva = valor.filter { c -> c.isDigit() }.take(5), errorClaveNueva = null, errorRecuperacion = null) }
    }

    fun onConfirmacionCambia(valor: String) {
        _uiState.update { it.copy(confirmacionClave = valor.filter { c -> c.isDigit() }.take(5), errorConfirmacion = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(claveVisible = !it.claveVisible) }
    }

    fun onErrorRecuperacion(mensaje: String) {
        _uiState.update { it.copy(errorRecuperacion = mensaje) }
    }

    fun onCuentaVerificada() {
        _uiState.update { it.copy(cuentaVerificada = true, errorRecuperacion = null) }
    }

    fun onVerificar(): DatosRecuperacion? {
        val estado = _uiState.value
        val errorCorreo = when {
            estado.correo.isBlank() -> "Ingresa el correo de tu cuenta"
            !correoValido(estado.correo) -> "Ingresa un correo válido, por ejemplo nombre@gmail.com"
            else -> null
        }
        val errorRut = when {
            estado.rut.isBlank() -> "Ingresa tu RUT"
            !validarRut(estado.rut) -> "El RUT no es válido: revisa el dígito verificador"
            else -> null
        }
        _uiState.update { it.copy(errorCorreo = errorCorreo, errorRut = errorRut, errorRecuperacion = null) }
        return if (errorCorreo != null || errorRut != null) null else DatosRecuperacion(estado.correo, estado.rut.trim())
    }

    fun onGuardarClave(): ClaveRecuperada? {
        val estado = _uiState.value
        val errorClave = errorClaveNueva(estado.claveNueva)
        val errorConfirmacion = errorConfirmacionClave(estado.claveNueva, estado.confirmacionClave)
        _uiState.update { it.copy(errorClaveNueva = errorClave, errorConfirmacion = errorConfirmacion, errorRecuperacion = null) }
        if (!estado.cuentaVerificada || errorClave != null || errorConfirmacion != null) return null
        return ClaveRecuperada(estado.correo, estado.rut.trim(), estado.claveNueva)
    }

    fun onLimpiar() {
        _uiState.value = RecuperarClaveUiState()
    }
}
